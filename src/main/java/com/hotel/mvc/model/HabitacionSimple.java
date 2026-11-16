package com.hotel.mvc.model;

/**
 * Subclase concreta HabitacionSimple (Herencia y Polimorfismo).
 * Regla de negocio: Costo = precioNoche * noches (+10% en temporada alta).
 */
public class HabitacionSimple extends Habitacion {

    public static final double RECARGO_TEMPORADA_ALTA = 0.10;

    public HabitacionSimple(String codigoHabitacion, int numero, int capacidad,
                            double precioNoche, EstadoHabitacion estado, String comodidades) {
        super(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
    }

    public HabitacionSimple(String codigoHabitacion, int numero, int capacidad, double precioNoche) {
        super(codigoHabitacion, numero, capacidad, precioNoche, EstadoHabitacion.DISPONIBLE, null);
    }

    @Override
    public String getTipo() {
        return "SIMPLE";
    }

    @Override
    public double getCargoExtraNoche() {
        return 0.00;
    }

    @Override
    public String getComodidadesPorDefecto() {
        return "Cama Individual, Wi-Fi, TV Cable, Baño Privado";
    }

    @Override
    public double calcularCosto(int noches, boolean temporadaAlta) {
        if (noches <= 0) {
            throw new IllegalArgumentException("El número de noches debe ser mayor que cero.");
        }
        double total = getPrecioNoche() * noches;
        if (temporadaAlta) {
            total *= (1.0 + RECARGO_TEMPORADA_ALTA);
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
