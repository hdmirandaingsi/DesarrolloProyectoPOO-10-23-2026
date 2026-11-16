package com.hotel.mvc.model;

/**
 * Subclase concreta HabitacionSuite (Herencia y Polimorfismo).
 * Regla de negocio:
 * Base = precioNoche * noches (+25% en temporada alta sobre la base) + Servicio Premium ($30.00 por noche).
 */
public class HabitacionSuite extends Habitacion {

    public static final double TASA_SERVICIO_PREMIUM_NOCHE = 30.00;
    public static final double RECARGO_TEMPORADA_ALTA = 0.25;

    public HabitacionSuite(String codigoHabitacion, int numero, int capacidad,
                           double precioNoche, EstadoHabitacion estado, String comodidades) {
        super(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
    }

    public HabitacionSuite(String codigoHabitacion, int numero, int capacidad, double precioNoche) {
        super(codigoHabitacion, numero, capacidad, precioNoche, EstadoHabitacion.DISPONIBLE, null);
    }

    @Override
    public String getTipo() {
        return "SUITE";
    }

    @Override
    public double getCargoExtraNoche() {
        return TASA_SERVICIO_PREMIUM_NOCHE;
    }

    @Override
    public String getComodidadesPorDefecto() {
        return "Cama King, Jacuzzi, Sala Ejecutiva, Servicio Premium ($30/noche)";
    }

    @Override
    public double calcularCosto(int noches, boolean temporadaAlta) {
        if (noches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor que cero.");
        }
        double base = getPrecioNoche() * noches;
        if (temporadaAlta) {
            base *= (1.0 + RECARGO_TEMPORADA_ALTA);
        }
        double total = base + (TASA_SERVICIO_PREMIUM_NOCHE * noches);
        return Math.round(total * 100.0) / 100.0;
    }
}
