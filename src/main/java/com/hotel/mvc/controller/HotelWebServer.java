package com.hotel.mvc.controller;

import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.Reserva;
import com.hotel.mvc.model.Usuario;
import com.hotel.mvc.util.SecurityAndValidationUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servidor Web embebido nativo del JDK (com.sun.net.httpserver.HttpServer).
 * Sirve la Vista Web (HTML5, CSS3, JavaScript) y expone los endpoints REST del Controlador MVC.
 */
public class HotelWebServer {

    private final HotelController controller;
    private final int port;
    private final File webRoot;
    private HttpServer server;

    public HotelWebServer(HotelController controller, int port, File webRoot) {
        this.controller = controller;
        this.port = port;
        this.webRoot = webRoot;
    }

    public void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Archivos estáticos de la Vista Web (HTML/CSS/JS)
        server.createContext("/", new StaticFileHandler(webRoot));

        // API REST de Autenticación (RF-01 Login y RF-06 Logout)
        server.createContext("/api/login", this::handleLogin);
        server.createContext("/api/logout", this::handleLogout);
        server.createContext("/api/session", this::handleSession);

        // API REST de Resumen y Procesos 1 a 5 (Protegidos por Token de Sesión)
        server.createContext("/api/dashboard", this::handleDashboard);
        server.createContext("/api/clientes", this::handleClientes);
        server.createContext("/api/habitaciones", this::handleHabitaciones);
        server.createContext("/api/disponibilidad", this::handleDisponibilidad);
        server.createContext("/api/cotizar", this::handleCotizar);
        server.createContext("/api/reservas", this::handleReservas);

        server.setExecutor(null);
        server.start();
    }

    public void detener() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleLogin(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            enviarJson(ex, 405, "{\"ok\":false,\"error\":\"Método no permitido\"}");
            return;
        }
        try {
            Map<String, String> params = leerParametros(ex);
            Usuario u = controller.iniciarSesion(params.get("username"), params.get("password"));
            String token = controller.crearTokenSesionWeb(u);
            enviarJson(ex, 200, String.format("{\"ok\":true,\"token\":\"%s\",\"usuario\":%s}", token, u.toJson()));
        } catch (Exception e) {
            enviarJson(ex, 401, "{\"ok\":false,\"error\":\"" + SecurityAndValidationUtil.escaparJson(e.getMessage()) + "\"}");
        }
    }

    private void handleLogout(HttpExchange ex) throws IOException {
        String token = obtenerToken(ex);
        controller.cerrarSesionWeb(token);
        enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Sesión cerrada correctamente.\"}");
    }

    private void handleSession(HttpExchange ex) throws IOException {
        Optional<Usuario> opt = autenticarPeticion(ex);
        if (!opt.isPresent()) {
            return;
        }
        enviarJson(ex, 200, "{\"ok\":true,\"usuario\":" + opt.get().toJson() + "}");
    }

    private void handleDashboard(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            enviarJson(ex, 200, "{\"ok\":true,\"resumen\":" + controller.obtenerResumenDashboardJson() + "}");
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private void handleClientes(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            Map<String, String> params = leerParametros(ex);
            String metodo = ex.getRequestMethod().toUpperCase();
            if ("GET".equals(metodo)) {
                String q = params.get("q");
                List<Cliente> lista = controller.buscarClientes(q);
                String jsonArray = lista.stream().map(Cliente::toJson).collect(Collectors.joining(",", "[", "]"));
                enviarJson(ex, 200, "{\"ok\":true,\"clientes\":" + jsonArray + "}");
            } else if ("POST".equals(metodo)) {
                String accion = params.getOrDefault("accion", "crear");
                if ("crear".equalsIgnoreCase(accion)) {
                    Cliente c = controller.registrarCliente(
                        params.get("codigo"), params.get("nombre"), params.get("documento"),
                        params.get("telefono"), params.get("email")
                    );
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Cliente registrado exitosamente.\",\"cliente\":" + c.toJson() + "}");
                } else if ("actualizar".equalsIgnoreCase(accion)) {
                    boolean activo = !"false".equalsIgnoreCase(params.getOrDefault("activo", "true"));
                    Cliente c = controller.modificarCliente(
                        params.get("codigo"), params.get("nombre"), params.get("documento"),
                        params.get("telefono"), params.get("email"), activo
                    );
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Cliente actualizado correctamente.\",\"cliente\":" + c.toJson() + "}");
                } else if ("eliminar".equalsIgnoreCase(accion)) {
                    controller.eliminarCliente(params.get("codigo"));
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Cliente eliminado correctamente.\"}");
                } else {
                    throw new IllegalArgumentException("Acción de cliente desconocida: " + accion);
                }
            }
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private void handleHabitaciones(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            Map<String, String> params = leerParametros(ex);
            String metodo = ex.getRequestMethod().toUpperCase();
            if ("GET".equals(metodo)) {
                String q = params.get("q");
                List<Habitacion> lista = controller.buscarHabitaciones(q);
                String jsonArray = lista.stream().map(Habitacion::toJson).collect(Collectors.joining(",", "[", "]"));
                enviarJson(ex, 200, "{\"ok\":true,\"habitaciones\":" + jsonArray + "}");
            } else if ("POST".equals(metodo)) {
                String accion = params.getOrDefault("accion", "crear");
                if ("crear".equalsIgnoreCase(accion) || "actualizar".equalsIgnoreCase(accion)) {
                    int numero = parsearEntero(params.get("numero"), "Número de habitación");
                    int capacidad = parsearEntero(params.get("capacidad"), "Capacidad");
                    double precio = parsearDecimal(params.get("precio"), "Precio por noche");
                    EstadoHabitacion estado = EstadoHabitacion.valueOf(params.getOrDefault("estado", "DISPONIBLE").toUpperCase());
                    Habitacion h;
                    if ("crear".equalsIgnoreCase(accion)) {
                        h = controller.registrarHabitacion(
                            params.get("codigo"), numero, params.get("tipo"), capacidad, precio, estado, params.get("comodidades")
                        );
                        enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Habitación registrada correctamente.\",\"habitacion\":" + h.toJson() + "}");
                    } else {
                        h = controller.modificarHabitacion(
                            params.get("codigo"), numero, params.get("tipo"), capacidad, precio, estado, params.get("comodidades")
                        );
                        enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Habitación modificada correctamente.\",\"habitacion\":" + h.toJson() + "}");
                    }
                } else if ("estado".equalsIgnoreCase(accion)) {
                    EstadoHabitacion nuevo = EstadoHabitacion.valueOf(params.get("estado").toUpperCase());
                    controller.cambiarEstadoHabitacion(params.get("codigo"), nuevo);
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Estado de habitación actualizado a " + nuevo.name() + ".\"}");
                } else if ("eliminar".equalsIgnoreCase(accion)) {
                    controller.eliminarHabitacion(params.get("codigo"));
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Habitación eliminada correctamente.\"}");
                } else {
                    throw new IllegalArgumentException("Acción de habitación no válida: " + accion);
                }
            }
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private void handleDisponibilidad(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            Map<String, String> params = leerParametros(ex);
            LocalDate entrada = parsearFecha(params.get("entrada"), "Fecha de entrada");
            LocalDate salida = parsearFecha(params.get("salida"), "Fecha de salida");
            int cap = params.containsKey("capacidad") && !params.get("capacidad").trim().isEmpty()
                    ? parsearEntero(params.get("capacidad"), "Capacidad") : 1;
            String tipo = params.getOrDefault("tipo", "TODOS");
            List<Habitacion> disp = controller.consultarDisponibilidadPorPeriodo(entrada, salida, cap, tipo);
            String jsonArray = disp.stream().map(Habitacion::toJson).collect(Collectors.joining(",", "[", "]"));
            enviarJson(ex, 200, "{\"ok\":true,\"disponibles\":" + jsonArray + "}");
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private void handleCotizar(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            Map<String, String> params = leerParametros(ex);
            LocalDate entrada = parsearFecha(params.get("entrada"), "Fecha de entrada");
            LocalDate salida = parsearFecha(params.get("salida"), "Fecha de salida");
            boolean tempAlta = "true".equalsIgnoreCase(params.get("temporadaAlta")) || "1".equals(params.get("temporadaAlta"));
            String cotizacion = controller.cotizarEstanciaJson(params.get("codigoHabitacion"), entrada, salida, tempAlta);
            enviarJson(ex, 200, "{\"ok\":true,\"cotizacion\":" + cotizacion + "}");
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private void handleReservas(HttpExchange ex) throws IOException {
        if (!autenticarPeticion(ex).isPresent()) return;
        try {
            Map<String, String> params = leerParametros(ex);
            String metodo = ex.getRequestMethod().toUpperCase();
            if ("GET".equals(metodo)) {
                String q = params.get("q");
                String estado = params.get("estado");
                List<Reserva> lista = controller.buscarReservas(q, estado);
                String jsonArray = lista.stream().map(Reserva::toJson).collect(Collectors.joining(",", "[", "]"));
                enviarJson(ex, 200, "{\"ok\":true,\"reservas\":" + jsonArray + "}");
            } else if ("POST".equals(metodo)) {
                String accion = params.getOrDefault("accion", "crear");
                if ("crear".equalsIgnoreCase(accion)) {
                    LocalDate entrada = parsearFecha(params.get("entrada"), "Fecha de entrada");
                    LocalDate salida = parsearFecha(params.get("salida"), "Fecha de salida");
                    int huespedes = parsearEntero(params.get("huespedes"), "Cantidad de huéspedes");
                    boolean tempAlta = "true".equalsIgnoreCase(params.get("temporadaAlta")) || "1".equals(params.get("temporadaAlta"));
                    Reserva r = controller.registrarReserva(
                        params.get("codigo"), params.get("codigoCliente"), params.get("codigoHabitacion"),
                        entrada, salida, huespedes, tempAlta
                    );
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Reserva confirmada exitosamente.\",\"reserva\":" + r.toJson() + "}");
                } else if ("actualizar".equalsIgnoreCase(accion)) {
                    LocalDate entrada = parsearFecha(params.get("entrada"), "Fecha de entrada");
                    LocalDate salida = parsearFecha(params.get("salida"), "Fecha de salida");
                    int huespedes = parsearEntero(params.get("huespedes"), "Cantidad de huéspedes");
                    boolean tempAlta = "true".equalsIgnoreCase(params.get("temporadaAlta")) || "1".equals(params.get("temporadaAlta"));
                    Reserva r = controller.modificarReserva(
                        params.get("codigo"), params.get("codigoHabitacion"), entrada, salida, huespedes, tempAlta
                    );
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Reserva actualizada correctamente.\",\"reserva\":" + r.toJson() + "}");
                } else if ("cancelar".equalsIgnoreCase(accion)) {
                    Reserva r = controller.cancelarReserva(params.get("codigo"));
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Reserva cancelada y habitación liberada.\",\"reserva\":" + r.toJson() + "}");
                } else if ("completar".equalsIgnoreCase(accion)) {
                    Reserva r = controller.completarReserva(params.get("codigo"));
                    enviarJson(ex, 200, "{\"ok\":true,\"mensaje\":\"Reserva marcada como COMPLETADA.\",\"reserva\":" + r.toJson() + "}");
                } else {
                    throw new IllegalArgumentException("Acción de reserva desconocida: " + accion);
                }
            }
        } catch (Exception e) {
            enviarError(ex, e);
        }
    }

    private Optional<Usuario> autenticarPeticion(HttpExchange ex) throws IOException {
        String token = obtenerToken(ex);
        Optional<Usuario> opt = controller.validarTokenWeb(token);
        if (!opt.isPresent()) {
            enviarJson(ex, 401, "{\"ok\":false,\"error\":\"Sesión expirada o no autenticada. Inicie sesión nuevamente.\"}");
        }
        return opt;
    }

    private String obtenerToken(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("X-Auth-Token");
        if (header != null && !header.trim().isEmpty()) {
            return header.trim();
        }
        String query = ex.getRequestURI().getRawQuery();
        if (query != null) {
            Map<String, String> map = parsearUrlEncoded(query);
            return map.get("token");
        }
        return null;
    }

    private Map<String, String> leerParametros(HttpExchange ex) throws IOException {
        Map<String, String> params = new HashMap<>();
        String query = ex.getRequestURI().getRawQuery();
        if (query != null && !query.isEmpty()) {
            params.putAll(parsearUrlEncoded(query));
        }
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = leerCuerpo(ex.getRequestBody());
            if (!body.isEmpty()) {
                params.putAll(parsearUrlEncoded(body));
            }
        }
        return params;
    }

    private String leerCuerpo(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parsearUrlEncoded(String raw) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private int parsearEntero(String val, String campo) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser un número entero válido.");
        }
    }

    private double parsearDecimal(String val, String campo) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser un valor numérico válido.");
        }
    }

    private LocalDate parsearFecha(String val, String campo) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio (formato YYYY-MM-DD).");
        }
        try {
            return LocalDate.parse(val.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("El campo '" + campo + "' tiene un formato de fecha inválido. Use YYYY-MM-DD.");
        }
    }

    private void enviarError(HttpExchange ex, Exception e) throws IOException {
        enviarJson(ex, 400, "{\"ok\":false,\"error\":\"" + SecurityAndValidationUtil.escaparJson(e.getMessage()) + "\"}");
    }

    private void enviarJson(HttpExchange ex, int status, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final File root;

        public StaticFileHandler(File root) {
            this.root = root;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                ex.sendResponseHeaders(403, -1);
                return;
            }
            File file = new File(root, path.substring(1));
            if (!file.exists() || file.isDirectory()) {
                byte[] notFound = "404 - Recurso no encontrado".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(notFound);
                }
                return;
            }
            String mime = "text/plain; charset=UTF-8";
            if (path.endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) mime = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".png")) mime = "image/png";

            byte[] bytes = Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", mime);
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
