package com.hotel.mvc.view;

import com.hotel.mvc.controller.HotelController;
import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.Reserva;
import com.hotel.mvc.model.Usuario;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

/**
 * Vista interactiva en Consola CLI (Control Total desde Terminal) conectada al mismo HotelController MVC.
 * Exige autenticación previa (RF-01 Login) y permite operar todos los procesos 1 a 5 desde la terminal.
 */
public class ConsolaCLIView {

    private final HotelController controller;
    private final Scanner scanner;

    public ConsolaCLIView(HotelController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    public void iniciarLoopInteractivo(int webPort) {
        System.out.println("=====================================================================================");
        System.out.println("  UNIVERSIDAD DE EL SALVADOR - FACULTAD DE INGENIERÍA Y ARQUITECTURA (POO115)");
        System.out.println("  HOTEL GRAN SOL & MONTAÑA - SISTEMA MVC (CLI + SERVIDOR WEB JDK + MYSQL 8.0)");
        System.out.println("  Vista Web HTML/CSS/JS activa en: http://localhost:" + webPort);
        System.out.println("=====================================================================================");

        boolean salir = false;
        while (!salir) {
            if (!controller.haySesionLocalActiva()) {
                salir = mostrarMenuLogin();
            } else {
                salir = mostrarMenuPrincipal(webPort);
            }
        }
    }

    private boolean mostrarMenuLogin() {
        System.out.println("\n--- [RF-01] MÓDULO DE INICIO DE SESIÓN (LOGIN PROTEGIDO) ---");
        System.out.println("1. Iniciar sesión con una cuenta configurada en la base de datos");
        System.out.println("0. Detener servidor y salir del sistema");
        System.out.print("Seleccione una opción: ");
        String op = scanner.nextLine().trim();
        if ("0".equals(op)) {
            return true;
        }
        if ("1".equals(op)) {
            try {
                System.out.print("Usuario o Correo: ");
                String user = scanner.nextLine().trim();
                System.out.print("Contraseña: ");
                String pass = scanner.nextLine().trim();
                Usuario u = controller.iniciarSesion(user, pass);
                System.out.println("[ACCESO CONCEDIDO] Bienvenido/a " + u.getNombreCompleto() + " (" + u.getRol() + ")");
            } catch (Exception e) {
                System.out.println("[ERROR DE AUTENTICACIÓN] " + e.getMessage());
            }
        }
        return false;
    }

    private boolean mostrarMenuPrincipal(int webPort) {
        Usuario u = controller.getUsuarioSesionLocal();
        System.out.println("\n=====================================================================================");
        System.out.println("  MENÚ PRINCIPAL MVC | Usuario: " + u.getUsername() + " (" + u.getRol() + ") | Web: http://localhost:" + webPort);
        System.out.println("=====================================================================================");
        System.out.println("  [Proceso 1 - Clientes]     1. Listar Clientes   | 2. Buscar Cliente  | 3. Registrar | 4. Modificar | 5. Eliminar");
        System.out.println("  [Proceso 2 - Habitaciones] 6. Listar Habitac.   | 7. Registrar Hab.  | 8. Cambiar Estado | 9. Eliminar Hab.");
        System.out.println("  [Proceso 3 - Disponib.]   10. Consultar Disponibilidad por Período de Fechas");
        System.out.println("  [Proceso 4 - Reservas]    11. Listar Reservas   | 12. Crear Reserva  | 13. Modificar | 14. Cancelar Reserva");
        System.out.println("  [Proceso 5 - Costos]      15. Calcular / Cotizar Costo Polimórfico de Reserva");
        System.out.println("  [RF-06 - Sesión]          99. Cerrar Sesión (Logout) | 0. Salir del Sistema");
        System.out.print("Seleccione una opción: ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1":
                    imprimirClientes(controller.listarClientes());
                    break;
                case "2":
                    System.out.print("Ingrese código, documento o nombre a buscar: ");
                    imprimirClientes(controller.buscarClientes(scanner.nextLine()));
                    break;
                case "3":
                    cliRegistrarCliente();
                    break;
                case "4":
                    cliModificarCliente();
                    break;
                case "5":
                    System.out.print("Código del cliente a eliminar: ");
                    controller.eliminarCliente(scanner.nextLine());
                    System.out.println("[OK] Cliente eliminado de MySQL.");
                    break;
                case "6":
                    imprimirHabitaciones(controller.listarHabitaciones());
                    break;
                case "7":
                    cliRegistrarHabitacion();
                    break;
                case "8":
                    cliCambiarEstadoHabitacion();
                    break;
                case "9":
                    System.out.print("Código de habitación a eliminar: ");
                    controller.eliminarHabitacion(scanner.nextLine());
                    System.out.println("[OK] Habitación eliminada de MySQL.");
                    break;
                case "10":
                    cliConsultarDisponibilidad();
                    break;
                case "11":
                    imprimirReservas(controller.listarReservas());
                    break;
                case "12":
                    cliRegistrarReserva();
                    break;
                case "13":
                    cliModificarReserva();
                    break;
                case "14":
                    System.out.print("Código de reserva a cancelar: ");
                    Reserva canc = controller.cancelarReserva(scanner.nextLine());
                    System.out.println("[OK] Reserva cancelada: " + canc);
                    break;
                case "15":
                    cliCalcularCosto();
                    break;
                case "99":
                    controller.cerrarSesionLocal();
                    System.out.println("[SESIÓN CERRADA] Ha regresado a la pantalla de Login.");
                    break;
                case "0":
                    return true;
                default:
                    System.out.println("Opción no reconocida.");
            }
        } catch (Exception e) {
            System.out.println("[VALIDACIÓN / ERROR] " + e.getMessage());
        }
        return false;
    }

    private void cliRegistrarCliente() {
        System.out.print("Código único (ej. CLI-009): ");
        String cod = scanner.nextLine();
        System.out.print("Nombre completo: ");
        String nom = scanner.nextLine();
        System.out.print("Documento (DUI/DNI): ");
        String doc = scanner.nextLine();
        System.out.print("Teléfono: ");
        String tel = scanner.nextLine();
        System.out.print("Correo electrónico: ");
        String email = scanner.nextLine();
        Cliente c = controller.registrarCliente(cod, nom, doc, tel, email);
        System.out.println("[OK] Cliente registrado en MySQL: " + c);
    }

    private void cliModificarCliente() {
        System.out.print("Código del cliente a modificar: ");
        String cod = scanner.nextLine();
        System.out.print("Nuevo nombre completo: ");
        String nom = scanner.nextLine();
        System.out.print("Nuevo documento: ");
        String doc = scanner.nextLine();
        System.out.print("Nuevo teléfono: ");
        String tel = scanner.nextLine();
        System.out.print("Nuevo correo electrónico: ");
        String email = scanner.nextLine();
        Cliente c = controller.modificarCliente(cod, nom, doc, tel, email, true);
        System.out.println("[OK] Cliente actualizado: " + c);
    }

    private void cliRegistrarHabitacion() {
        System.out.print("Código único (ej. HAB-104): ");
        String cod = scanner.nextLine();
        System.out.print("Número de habitación (entero): ");
        int num = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Tipo (SIMPLE / DOBLE / SUITE): ");
        String tipo = scanner.nextLine();
        System.out.print("Capacidad máxima (personas): ");
        int cap = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Precio base por noche ($): ");
        double precio = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Comodidades (Enter para predeterminadas): ");
        String com = scanner.nextLine();
        Habitacion h = controller.registrarHabitacion(cod, num, tipo, cap, precio, EstadoHabitacion.DISPONIBLE, com);
        System.out.println("[OK] Habitación registrada: " + h);
    }

    private void cliCambiarEstadoHabitacion() {
        System.out.print("Código de habitación: ");
        String cod = scanner.nextLine();
        System.out.print("Nuevo estado (DISPONIBLE / OCUPADA / FUERA_DE_SERVICIO): ");
        EstadoHabitacion est = EstadoHabitacion.valueOf(scanner.nextLine().trim().toUpperCase());
        controller.cambiarEstadoHabitacion(cod, est);
        System.out.println("[OK] Estado actualizado correctamente.");
    }

    private void cliConsultarDisponibilidad() {
        System.out.print("Fecha de entrada (YYYY-MM-DD): ");
        LocalDate entrada = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Fecha de salida (YYYY-MM-DD): ");
        LocalDate salida = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Capacidad mínima requerida (Enter para 1): ");
        String capStr = scanner.nextLine().trim();
        int cap = capStr.isEmpty() ? 1 : Integer.parseInt(capStr);
        List<Habitacion> disp = controller.consultarDisponibilidadPorPeriodo(entrada, salida, cap, "TODOS");
        System.out.println("--- Habitaciones disponibles entre " + entrada + " y " + salida + " ---");
        imprimirHabitaciones(disp);
    }

    private void cliRegistrarReserva() {
        System.out.print("Código de reserva (Enter para autogenerar): ");
        String cod = scanner.nextLine();
        System.out.print("Código de cliente (ej. CLI-001): ");
        String cli = scanner.nextLine();
        System.out.print("Código de habitación (ej. HAB-102): ");
        String hab = scanner.nextLine();
        System.out.print("Fecha de entrada (YYYY-MM-DD): ");
        LocalDate entrada = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Fecha de salida (YYYY-MM-DD): ");
        LocalDate salida = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Cantidad de huéspedes: ");
        int huesp = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("¿Es temporada alta? (s/n): ");
        boolean ta = scanner.nextLine().trim().equalsIgnoreCase("s");
        Reserva r = controller.registrarReserva(cod, cli, hab, entrada, salida, huesp, ta);
        System.out.println("[OK] Reserva registrada en MySQL:\n  " + r);
    }

    private void cliModificarReserva() {
        System.out.print("Código de reserva a modificar: ");
        String cod = scanner.nextLine();
        System.out.print("Nuevo código de habitación: ");
        String hab = scanner.nextLine();
        System.out.print("Nueva fecha de entrada (YYYY-MM-DD): ");
        LocalDate entrada = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Nueva fecha de salida (YYYY-MM-DD): ");
        LocalDate salida = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Nueva cantidad de huéspedes: ");
        int huesp = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("¿Es temporada alta? (s/n): ");
        boolean ta = scanner.nextLine().trim().equalsIgnoreCase("s");
        Reserva r = controller.modificarReserva(cod, hab, entrada, salida, huesp, ta);
        System.out.println("[OK] Reserva actualizada:\n  " + r);
    }

    private void cliCalcularCosto() {
        System.out.print("Código de habitación (ej. HAB-201): ");
        String codHab = scanner.nextLine().trim();
        Habitacion h = controller.buscarHabitacionPorCodigo(codHab)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada: " + codHab));
        System.out.print("Fecha de entrada (YYYY-MM-DD): ");
        LocalDate entrada = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Fecha de salida (YYYY-MM-DD): ");
        LocalDate salida = LocalDate.parse(scanner.nextLine().trim());
        Reserva.validarPeriodo(entrada, salida);
        System.out.print("¿Temporada alta? (s/n): ");
        boolean ta = scanner.nextLine().trim().equalsIgnoreCase("s");
        int noches = (int) ChronoUnit.DAYS.between(entrada, salida);
        System.out.println("--- Desglose de Costo Polimórfico (Proceso 5) ---");
        System.out.printf("  Habitación: #%d (%s) | Noches: %d | Tarifa Base: $%.2f/noche%n",
                h.getNumero(), h.getTipo(), noches, h.getPrecioNoche());
        System.out.printf("  Subtotal Base (%d x $%.2f): $%.2f%n", noches, h.getPrecioNoche(), h.calcularSubtotalBase(noches));
        System.out.printf("  Cargos Adicionales (Categoría/Temp. Alta): $%.2f%n", h.calcularCargosAdicionales(noches, ta));
        System.out.printf("  COSTO TOTAL DE RESERVA: $%.2f%n", h.calcularCosto(noches, ta));
    }

    private void imprimirClientes(List<Cliente> lista) {
        if (lista.isEmpty()) {
            System.out.println("  (Sin resultados de clientes)");
            return;
        }
        lista.forEach(c -> System.out.println("  " + c));
    }

    private void imprimirHabitaciones(List<Habitacion> lista) {
        if (lista.isEmpty()) {
            System.out.println("  (Sin resultados de habitaciones)");
            return;
        }
        lista.forEach(h -> System.out.println("  " + h));
    }

    private void imprimirReservas(List<Reserva> lista) {
        if (lista.isEmpty()) {
            System.out.println("  (Sin resultados de reservas)");
            return;
        }
        lista.forEach(r -> System.out.println("  " + r));
    }
}
