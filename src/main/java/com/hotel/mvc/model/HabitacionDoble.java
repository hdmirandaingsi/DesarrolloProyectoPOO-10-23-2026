package com.hotel.mvc.model;

/**
 * Subclase concreta HabitacionDoble (Herencia y Polimorfismo).
 * Regla de negocio (Enfoque 2 OOP):
 * Base = precioNoche * noches (+15% en temporada alta sobre la base) + Desayuno ($15.00 por noche).
 */
public class HabitacionDoble extends Habitacion {

    public static final double COSTO_DESAYUNO_NOCHE = 15.00;
    public static final double RECARGO_TEMPORADA_ALTA = 0.15;

    public HabitacionDoble(String codigoHabitacion, int numero, int capacidad,
                           double precioNoche, EstadoHabitacion estado, String comodidades) {
        super(codigoHabitacion, numero, capacidad, precioNoche, estado, comodidades);
    }

    public HabitacionDoble(String codigoHabitacion, int numero, int capacidad, double precioNoche) {
        super(codigoHabitacion, numero, capacidad, precioNoche, EstadoHabitacion.DISPONIBLE, null);
    }

    @Override
    public String getTipo() {
        return "DOBLE";
    }

    @Override
    public double getCargoExtraNoche() {
        return COSTO_DESAYUNO_NOCHE;
    }

    @Override
    public String getComodidadesPorDefecto() {
        return "Dos Camas Matrimoniales, Desayuno ($15/noche), Frigobar, Wi-Fi";
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
        double total = base + (COSTO_DESAYUNO_NOCHE * noches);
        return Math.round(total * 100.0) / 100.0;
    }
}
