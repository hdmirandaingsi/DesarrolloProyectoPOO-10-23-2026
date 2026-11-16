package com.hotel.mvc.model;

/**
 * Fábrica polimórfica de Habitaciones (Patrón Factory Method).
 * Centraliza la creación de instancias de HabitacionSimple, HabitacionDoble y HabitacionSuite.
 */
public final class HabitacionFactory {

    private HabitacionFactory() {
    }

    public static Habitacion crearHabitacion(String tipo, String codigoHabitacion, int numero,
                                             int capacidad, double precioNoche,
                                             EstadoHabitacion estado, String comodidades) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio (SIMPLE, DOBLE o SUITE).");
        }
        switch (tipo.trim().toUpperCase()) {
            case "SIMPLE":
                return new HabitacionSimple(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
            case "DOBLE":
                return new HabitacionDoble(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
            case "SUITE":
                return new HabitacionSuite(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
            default:
                throw new IllegalArgumentException("Tipo de habitación no válido: '" + tipo + "'. Use SIMPLE, DOBLE o SUITE.");
        }
    }
}
