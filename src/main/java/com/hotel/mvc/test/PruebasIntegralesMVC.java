package com.hotel.mvc.test;

import com.hotel.mvc.controller.HotelController;
import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.EstadoReserva;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.HabitacionDoble;
import com.hotel.mvc.model.HabitacionSimple;
import com.hotel.mvc.model.HabitacionSuite;
import com.hotel.mvc.model.Reserva;
import java.time.LocalDate;
import java.util.List;

/**
 * Suite de Pruebas Automatizadas desde CLI para validar POO, MVC, Validaciones y Persistencia en MySQL.
 */
public class PruebasIntegralesMVC {

    private static int aprobadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=====================================================================================");
        System.out.println("  EJECUTANDO SUITE DE PRUEBAS INTEGRALES MVC + MYSQL 8.0 (CLAVE 1 - HOTEL)");
        System.out.println("=====================================================================================");

        HotelController controller = new HotelController();

        // 1. Validación local de credenciales obligatorias (sin cuenta de demostración)
        verificarExcepcion("RF-01 Rechazo de credenciales vacías", () -> {
            controller.iniciarSesion("", "");
        });

        // 2. Pruebas Polimórficas de Tarifación (Proceso 5 y Tabla 8 de Tarea de Investigación)
        verificar("Proceso 5 - Tarifa Simple 3 noches sin temp. alta ($150.00)", () -> {
            Habitacion h = new HabitacionSimple("HAB-T1", 901, 1, 50.0);
            return Math.abs(h.calcularCosto(3, false) - 150.00) < 0.01;
        });

        verificar("Proceso 5 - Tarifa Simple 3 noches con temp. alta ($165.00)", () -> {
            Habitacion h = new HabitacionSimple("HAB-T1", 901, 1, 50.0);
            return Math.abs(h.calcularCosto(3, true) - 165.00) < 0.01;
        });

        verificar("Proceso 5 - Tarifa Doble 2 noches sin temp. alta ($190.00)", () -> {
            Habitacion h = new HabitacionDoble("HAB-T2", 902, 2, 80.0);
            return Math.abs(h.calcularCosto(2, false) - 190.00) < 0.01;
        });

        verificar("Proceso 5 - Tarifa Doble 2 noches con temp. alta ($214.00 Enfoque 2 OOP)", () -> {
            Habitacion h = new HabitacionDoble("HAB-T2", 902, 2, 80.0);
            return Math.abs(h.calcularCosto(2, true) - 214.00) < 0.01;
        });

        verificar("Proceso 5 - Tarifa Suite 2 noches con temp. alta ($435.00)", () -> {
            Habitacion h = new HabitacionSuite("HAB-T3", 903, 4, 150.0);
            return Math.abs(h.calcularCosto(2, true) - 435.00) < 0.01;
        });

        // 3. Pruebas de Persistencia en MySQL (Mínimo 6 registros por tabla)
        verificar("RF-03 Persistencia MySQL cuenta con >= 6 clientes", () -> controller.listarClientes().size() >= 6);
        verificar("RF-03 Persistencia MySQL cuenta con >= 6 habitaciones", () -> controller.listarHabitaciones().size() >= 6);
        verificar("RF-03 Persistencia MySQL cuenta con >= 6 reservas", () -> controller.listarReservas().size() >= 6);

        // 4. Pruebas de Validación de Entrada e Integridad Referencial (Sección 1.4 y 1.5)
        verificarExcepcion("Validación 1.4 - Rechazo de cliente con código duplicado", () -> {
            controller.registrarCliente("CLI-001", "Duplicado", "99999999-9", "7000-0000", "dup@test.com");
        });

        verificarExcepcion("Validación 1.4 - Rechazo de correo con formato inválido", () -> {
            new Cliente("CLI-999", "Cliente de prueba", "DOC-TEST", "70000000", "correo_invalido");
        });

        verificarExcepcion("Validación 1.5 - Impedir eliminar cliente con reservas asociadas (CLI-001)", () -> {
            controller.eliminarCliente("CLI-001");
        });

        verificarExcepcion("Validación 1.5 - Impedir eliminar habitación con reservas asociadas (HAB-101)", () -> {
            controller.eliminarHabitacion("HAB-101");
        });

        verificarExcepcion("Validación 1.4 - Rechazo de fecha de salida anterior o igual a entrada", () -> {
            controller.consultarDisponibilidadPorPeriodo(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 18), 1, "TODOS");
        });

        verificarExcepcion("Validación 1.4 - Rechazo de superposición de fechas en habitación ocupada (HAB-101: 2026-11-15 a 2026-11-18)", () -> {
            controller.registrarReserva("RES-9901", "CLI-002", "HAB-101",
                    LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 19), 1, false);
        });

        verificarExcepcion("Validación 1.4 - Rechazo cuando cantidad de huéspedes supera capacidad de habitación", () -> {
            controller.registrarReserva("RES-9902", "CLI-002", "HAB-102",
                    LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), 5, false);
        });

        verificarExcepcion("Validación 1.5 - Rechazo de reserva en habitación FUERA_DE_SERVICIO (HAB-203)", () -> {
            controller.registrarReserva("RES-9903", "CLI-002", "HAB-203",
                    LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7), 2, false);
        });

        verificarExcepcion("Validación 1.5 - Rechazo al cancelar reserva ya CANCELADA (RES-1007)", () -> {
            controller.cancelarReserva("RES-1007");
        });

        verificarExcepcion("Hallazgo corregido - Rechazo al cancelar reserva ya COMPLETADA (RES-1006)", () -> {
            controller.cancelarReserva("RES-1006");
        });

        // 5. Prueba de Ciclo Completo CRUD en MySQL desde CLI (Crear -> Modificar -> Eliminar)
        verificar("Ciclo CRUD Cliente en MySQL (Registrar -> Modificar -> Eliminar sin reservas)", () -> {
            String cod = "CLI-TEST";
            try { controller.eliminarCliente(cod); } catch (Exception ignored) {}
            controller.registrarCliente(cod, "Cliente Prueba CLI", "88776655-4", "7111-2233", "prueba@ues.edu.sv");
            controller.modificarCliente(cod, "Cliente Prueba Modificado", "88776655-4", "7111-9999", "prueba@ues.edu.sv", true);
            controller.eliminarCliente(cod);
            return !controller.buscarClientePorCodigo(cod).isPresent();
        });

        // 6. Prueba de Consulta de Disponibilidad (Proceso 3)
        verificar("Proceso 3 - Consulta de disponibilidad excluye HAB-101 en [2026-11-15, 2026-11-18] y HAB-203 (Fuera de servicio)", () -> {
            List<Habitacion> disp = controller.consultarDisponibilidadPorPeriodo(
                    LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18), 1, "TODOS");
            boolean contiene101 = disp.stream().anyMatch(h -> "HAB-101".equals(h.getCodigoHabitacion()));
            boolean contiene203 = disp.stream().anyMatch(h -> "HAB-203".equals(h.getCodigoHabitacion()));
            boolean contiene102 = disp.stream().anyMatch(h -> "HAB-102".equals(h.getCodigoHabitacion()));
            return !contiene101 && !contiene203 && contiene102;
        });

        System.out.println("=====================================================================================");
        System.out.printf("  RESULTADO FINAL: %d aprobadas, %d fallidas (Total: %d)%n",
                aprobadas, fallidas, (aprobadas + fallidas));
        System.out.println("=====================================================================================");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    @FunctionalInterface
    private interface PruebaBooleana {
        boolean ejecutar() throws Exception;
    }

    @FunctionalInterface
    private interface PruebaConExcepcion {
        void ejecutar() throws Exception;
    }

    private static void verificar(String nombre, PruebaBooleana prueba) {
        try {
            if (prueba.ejecutar()) {
                aprobadas++;
                System.out.println("  [APROBADA] " + nombre);
            } else {
                fallidas++;
                System.out.println("  [FALLIDA]  " + nombre);
            }
        } catch (Exception e) {
            fallidas++;
            System.out.println("  [ERROR]    " + nombre + " -> " + e.getMessage());
        }
    }

    private static void verificarExcepcion(String nombre, PruebaConExcepcion prueba) {
        try {
            prueba.ejecutar();
            fallidas++;
            System.out.println("  [FALLIDA - No lanzó excepción] " + nombre);
        } catch (IllegalArgumentException | IllegalStateException expected) {
            aprobadas++;
            System.out.println("  [APROBADA] " + nombre + " -> (" + expected.getMessage() + ")");
        } catch (Exception e) {
            fallidas++;
            System.out.println("  [ERROR INESPERADO] " + nombre + " -> " + e.getMessage());
        }
    }
}
