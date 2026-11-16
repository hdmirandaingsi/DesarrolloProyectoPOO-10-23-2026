package com.hotel.mvc.controller;

import com.hotel.mvc.dao.ClienteDAO;
import com.hotel.mvc.dao.HabitacionDAO;
import com.hotel.mvc.dao.ReservaDAO;
import com.hotel.mvc.dao.UsuarioDAO;
import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.EstadoReserva;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.HabitacionFactory;
import com.hotel.mvc.model.Reserva;
import com.hotel.mvc.model.Usuario;
import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Controlador principal MVC (Patrón GRASP Controller + Facade).
 * Coordina el flujo entre las Vistas (Web HTML/CSS/JS, Java Swing y Consola CLI)
 * y el Modelo/DAO en MySQL, garantizando la autenticación de sesión y todas las validaciones.
 */
public class HotelController {

    private final UsuarioDAO usuarioDAO;
    private final ClienteDAO clienteDAO;
    private final HabitacionDAO habitacionDAO;
    private final ReservaDAO reservaDAO;

    // Sesiones activas (Token -> Usuario) y sesión actual para clientes de escritorio/CLI
    private final Map<String, Usuario> sesionesWeb = new ConcurrentHashMap<>();
    private Usuario usuarioSesionLocal;

    public HotelController() {
        this.usuarioDAO = new UsuarioDAO();
        this.clienteDAO = new ClienteDAO();
        this.habitacionDAO = new HabitacionDAO();
        this.reservaDAO = new ReservaDAO();
    }

    // ====================================================================================================
    // RF-01 y RF-06: INICIO Y CIERRE DE SESIÓN (LOGIN / LOGOUT)
    // ====================================================================================================
    public Usuario iniciarSesion(String usuarioOCorreo, String passwordPlano) {
        if (usuarioOCorreo == null || usuarioOCorreo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar su nombre de usuario o correo electrónico.");
        }
        if (passwordPlano == null || passwordPlano.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar su contraseña.");
        }
        Optional<Usuario> opt = usuarioDAO.buscarPorCredencial(usuarioOCorreo.trim());
        if (!opt.isPresent() || !opt.get().autenticar(passwordPlano)) {
            throw new IllegalArgumentException("Credenciales incorrectas. Verifique su usuario y contraseña.");
        }
        this.usuarioSesionLocal = opt.get();
        return this.usuarioSesionLocal;
    }

    public String crearTokenSesionWeb(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        sesionesWeb.put(token, usuario);
        return token;
    }

    public Optional<Usuario> validarTokenWeb(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sesionesWeb.get(token.trim()));
    }

    public void cerrarSesionWeb(String token) {
        if (token != null) {
            sesionesWeb.remove(token.trim());
        }
    }

    public void cerrarSesionLocal() {
        this.usuarioSesionLocal = null;
    }

    public Usuario getUsuarioSesionLocal() {
        return usuarioSesionLocal;
    }

    public boolean haySesionLocalActiva() {
        return usuarioSesionLocal != null;
    }

    // ====================================================================================================
    // PROCESO 1: GESTIÓN DE CLIENTES
    // ====================================================================================================
    public Cliente registrarCliente(String codigo, String nombre, String documento, String telefono, String email) {
        Cliente nuevo = new Cliente(codigo, nombre, documento, telefono, email, true);
        clienteDAO.insertar(nuevo);
        return nuevo;
    }

    public Cliente modificarCliente(String codigo, String nombre, String documento, String telefono, String email, boolean activo) {
        Cliente mod = new Cliente(codigo, nombre, documento, telefono, email, activo);
        clienteDAO.actualizar(mod);
        return mod;
    }

    public void eliminarCliente(String codigoCliente) {
        String limpio = SecurityAndValidationUtil.validarCodigo(codigoCliente, "Cliente");
        clienteDAO.eliminar(limpio);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }

    public List<Cliente> buscarClientes(String criterio) {
        return clienteDAO.buscarPorCriterio(criterio);
    }

    public Optional<Cliente> buscarClientePorCodigo(String codigo) {
        return clienteDAO.buscarPorCodigo(codigo);
    }

    // ====================================================================================================
    // PROCESO 2: GESTIÓN DE HABITACIONES
    // ====================================================================================================
    public Habitacion registrarHabitacion(String codigo, int numero, String tipo, int capacidad,
                                          double precioNoche, EstadoHabitacion estado, String comodidades) {
        Habitacion nueva = HabitacionFactory.crearHabitacion(tipo, codigo, numero, capacidad, precioNoche, estado, comodidades);
        habitacionDAO.insertar(nueva);
        return nueva;
    }

    public Habitacion modificarHabitacion(String codigo, int numero, String tipo, int capacidad,
                                          double precioNoche, EstadoHabitacion estado, String comodidades) {
        Habitacion mod = HabitacionFactory.crearHabitacion(tipo, codigo, numero, capacidad, precioNoche, estado, comodidades);
        habitacionDAO.actualizar(mod);
        return mod;
    }

    public void cambiarEstadoHabitacion(String codigoHabitacion, EstadoHabitacion nuevoEstado) {
        String limpio = SecurityAndValidationUtil.validarCodigo(codigoHabitacion, "Habitación");
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Debe indicar un estado válido (DISPONIBLE, OCUPADA o FUERA_DE_SERVICIO).");
        }
        habitacionDAO.cambiarEstado(limpio, nuevoEstado);
    }

    public void eliminarHabitacion(String codigoHabitacion) {
        String limpio = SecurityAndValidationUtil.validarCodigo(codigoHabitacion, "Habitación");
        habitacionDAO.eliminar(limpio);
    }

    public List<Habitacion> listarHabitaciones() {
        return habitacionDAO.listarTodas();
    }

    public List<Habitacion> buscarHabitaciones(String criterio) {
        return habitacionDAO.buscarPorCriterio(criterio);
    }

    public Optional<Habitacion> buscarHabitacionPorCodigo(String codigo) {
        return habitacionDAO.buscarPorCodigo(codigo);
    }

    // ====================================================================================================
    // PROCESO 3: CONSULTA DE DISPONIBILIDAD POR PERÍODO DE FECHAS Y CAPACIDAD
    // ====================================================================================================
    public List<Habitacion> consultarDisponibilidadPorPeriodo(LocalDate entrada, LocalDate salida,
                                                              int capacidadMinima, String tipoFiltro) {
        Reserva.validarPeriodo(entrada, salida);
        if (capacidadMinima < 0) {
            throw new IllegalArgumentException("La capacidad mínima no puede ser negativa.");
        }
        List<Habitacion> todas = habitacionDAO.listarTodas();
        List<Habitacion> disponibles = new ArrayList<>();
        for (Habitacion h : todas) {
            // 3.4: Excluir habitaciones FUERA_DE_SERVICIO
            if (h.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
                continue;
            }
            if (capacidadMinima > 0 && h.getCapacidad() < capacidadMinima) {
                continue;
            }
            if (tipoFiltro != null && !tipoFiltro.trim().isEmpty() && !"TODOS".equalsIgnoreCase(tipoFiltro.trim())) {
                if (!h.getTipo().equalsIgnoreCase(tipoFiltro.trim())) {
                    continue;
                }
            }
            // 3.3 y 3.4: Excluir habitaciones con reservas CONFIRMADAS que se superponen con [entrada, salida)
            List<Reserva> conflictos = reservaDAO.buscarConflictosHabitacion(h.getCodigoHabitacion(), entrada, salida, null);
            if (conflictos.isEmpty()) {
                disponibles.add(h);
            }
        }
        return disponibles;
    }

    // ====================================================================================================
    // PROCESO 4: GESTIÓN DE RESERVAS
    // ====================================================================================================
    public Reserva registrarReserva(String codigoReserva, String codigoCliente, String codigoHabitacion,
                                    LocalDate fechaEntrada, LocalDate fechaSalida,
                                    int cantidadHuespedes, boolean temporadaAlta) {
        String codRes = (codigoReserva == null || codigoReserva.trim().isEmpty())
                ? reservaDAO.generarSiguienteCodigoReserva()
                : SecurityAndValidationUtil.validarCodigo(codigoReserva, "Reserva");

        Cliente cliente = clienteDAO.buscarPorCodigo(codigoCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente con código '" + codigoCliente + "' no existe."));
        Habitacion habitacion = habitacionDAO.buscarPorCodigo(codigoHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("La habitación con código '" + codigoHabitacion + "' no existe."));

        Reserva.validarPeriodo(fechaEntrada, fechaSalida);
        List<Reserva> conflictos = reservaDAO.buscarConflictosHabitacion(habitacion.getCodigoHabitacion(), fechaEntrada, fechaSalida, null);
        if (!conflictos.isEmpty()) {
            Reserva conf = conflictos.get(0);
            throw new IllegalStateException(String.format(
                "La habitación #%d (%s) no tiene disponibilidad entre %s y %s: presenta conflicto con la reserva %s (%s a %s).",
                habitacion.getNumero(), habitacion.getCodigoHabitacion(), fechaEntrada, fechaSalida,
                conf.getCodigoReserva(), conf.getFechaEntrada(), conf.getFechaSalida()
            ));
        }

        Reserva nueva = new Reserva(codRes, cliente, habitacion, fechaEntrada, fechaSalida, cantidadHuespedes, temporadaAlta);
        reservaDAO.insertar(nueva);
        habitacionDAO.cambiarEstado(habitacion.getCodigoHabitacion(), EstadoHabitacion.OCUPADA);
        return nueva;
    }

    /**
     * Proceso 4.6: Modificar fechas, habitación, cantidad de huéspedes o temporada alta de una reserva activa.
     */
    public Reserva modificarReserva(String codigoReserva, String nuevoCodigoHabitacion,
                                    LocalDate nuevaEntrada, LocalDate nuevaSalida,
                                    int nuevosHuespedes, boolean nuevaTemporadaAlta) {
        Reserva existente = reservaDAO.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva con código: " + codigoReserva));

        Habitacion habAnterior = existente.getHabitacion();
        Habitacion habNueva = habitacionDAO.buscarPorCodigo(nuevoCodigoHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("No existe la habitación con código: " + nuevoCodigoHabitacion));

        Reserva.validarPeriodo(nuevaEntrada, nuevaSalida);
        List<Reserva> conflictos = reservaDAO.buscarConflictosHabitacion(
                habNueva.getCodigoHabitacion(), nuevaEntrada, nuevaSalida, existente.getCodigoReserva());
        if (!conflictos.isEmpty()) {
            Reserva conf = conflictos.get(0);
            throw new IllegalStateException(String.format(
                "Conflicto de fechas al modificar: la habitación #%d ya está reservada en [%s a %s] por %s.",
                habNueva.getNumero(), conf.getFechaEntrada(), conf.getFechaSalida(), conf.getCodigoReserva()
            ));
        }

        existente.actualizarDatosReserva(habNueva, nuevaEntrada, nuevaSalida, nuevosHuespedes, nuevaTemporadaAlta);
        reservaDAO.actualizar(existente);

        // Si cambió de habitación, sincronizar disponibilidad física de ambas habitaciones
        if (!habAnterior.getCodigoHabitacion().equalsIgnoreCase(habNueva.getCodigoHabitacion())) {
            sincronizarEstadoHabitacionSegunReservas(habAnterior.getCodigoHabitacion());
        }
        habitacionDAO.cambiarEstado(habNueva.getCodigoHabitacion(), EstadoHabitacion.OCUPADA);
        return existente;
    }

    /**
     * Proceso 4.5: Cancelar reserva verificando su existencia y estado actual.
     */
    public Reserva cancelarReserva(String codigoReserva) {
        Reserva reserva = reservaDAO.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva con código: " + codigoReserva));
        reserva.cancelar();
        reservaDAO.actualizar(reserva);
        sincronizarEstadoHabitacionSegunReservas(reserva.getHabitacion().getCodigoHabitacion());
        return reserva;
    }

    public Reserva completarReserva(String codigoReserva) {
        Reserva reserva = reservaDAO.buscarPorCodigo(codigoReserva)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva con código: " + codigoReserva));
        reserva.completar();
        reservaDAO.actualizar(reserva);
        sincronizarEstadoHabitacionSegunReservas(reserva.getHabitacion().getCodigoHabitacion());
        return reserva;
    }

    private void sincronizarEstadoHabitacionSegunReservas(String codigoHabitacion) {
        Optional<Habitacion> optHab = habitacionDAO.buscarPorCodigo(codigoHabitacion);
        if (!optHab.isPresent()) {
            return;
        }
        Habitacion h = optHab.get();
        if (h.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            return;
        }
        int activas = reservaDAO.contarReservasConfirmadasParaHabitacion(codigoHabitacion);
        EstadoHabitacion nuevo = (activas > 0) ? EstadoHabitacion.OCUPADA : EstadoHabitacion.DISPONIBLE;
        habitacionDAO.cambiarEstado(codigoHabitacion, nuevo);
    }

    public List<Reserva> listarReservas() {
        return reservaDAO.listarTodas();
    }

    public List<Reserva> buscarReservas(String criterio, String estado) {
        return reservaDAO.buscarPorCriterio(criterio, estado);
    }

    public Optional<Reserva> buscarReservaPorCodigo(String codigoReserva) {
        return reservaDAO.buscarPorCodigo(codigoReserva);
    }

    // ====================================================================================================
    // PROCESO 5: CÁLCULO DEL COSTO (COTIZADOR POLIMÓRFICO EN TIEMPO REAL)
    // ====================================================================================================
    public String cotizarEstanciaJson(String codigoHabitacion, LocalDate entrada, LocalDate salida, boolean temporadaAlta) {
        Habitacion hab = habitacionDAO.buscarPorCodigo(codigoHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("No existe la habitación con código: " + codigoHabitacion));
        Reserva.validarPeriodo(entrada, salida);
        int noches = (int) ChronoUnit.DAYS.between(entrada, salida);
        double tarifaBase = hab.getPrecioNoche();
        double subtotalBase = hab.calcularSubtotalBase(noches);
        double cargosExtra = hab.calcularCargosAdicionales(noches, temporadaAlta);
        double total = hab.calcularCosto(noches, temporadaAlta);
        boolean disponiblePeriodo = hab.getEstado() != EstadoHabitacion.FUERA_DE_SERVICIO
                && reservaDAO.buscarConflictosHabitacion(hab.getCodigoHabitacion(), entrada, salida, null).isEmpty();

        return String.format(Locale.US,
            "{\"codigoHabitacion\":\"%s\",\"numero\":%d,\"tipo\":\"%s\",\"capacidad\":%d,\"noches\":%d,\"temporadaAlta\":%b,\"tarifaBaseNoche\":%.2f,\"subtotalBase\":%.2f,\"cargosAdicionales\":%.2f,\"costoTotal\":%.2f,\"disponibleEnPeriodo\":%b}",
            hab.getCodigoHabitacion(), hab.getNumero(), hab.getTipo(), hab.getCapacidad(),
            noches, temporadaAlta, tarifaBase, subtotalBase, cargosExtra, total, disponiblePeriodo
        );
    }

    public String obtenerResumenDashboardJson() {
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();
        List<Reserva> reservas = reservaDAO.listarTodas();

        long habDisponibles = habitaciones.stream().filter(h -> h.getEstado() == EstadoHabitacion.DISPONIBLE).count();
        long habOcupadas = habitaciones.stream().filter(h -> h.getEstado() == EstadoHabitacion.OCUPADA).count();
        long habFuera = habitaciones.stream().filter(h -> h.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO).count();
        long resConfirmadas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA).count();
        long resCanceladas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.CANCELADA).count();
        long resCompletadas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.COMPLETADA).count();
        double ingresosConfirmados = reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .mapToDouble(Reserva::getCostoTotal)
                .sum();

        return String.format(Locale.US,
            "{\"totalClientes\":%d,\"totalHabitaciones\":%d,\"habDisponibles\":%d,\"habOcupadas\":%d,\"habFueraServicio\":%d,\"totalReservas\":%d,\"resConfirmadas\":%d,\"resCanceladas\":%d,\"resCompletadas\":%d,\"ingresosTotales\":%.2f,\"siguienteCodigoReserva\":\"%s\"}",
            clientes.size(), habitaciones.size(), habDisponibles, habOcupadas, habFuera,
            reservas.size(), resConfirmadas, resCanceladas, resCompletadas, ingresosConfirmados,
            reservaDAO.generarSiguienteCodigoReserva()
        );
    }
}
