package com.hotel.mvc.model;

import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.util.Locale;
import java.util.Objects;

/**
 * Superclase abstracta Habitacion (Proceso 2: Gestión de Habitaciones y Proceso 5: Cálculo Polimórfico).
 * Modela los atributos comunes (código único, número, capacidad, precio por noche, estado)
 * y declara el contrato abstracto calcularCosto() para despacho polimórfico sin condicionales por tipo.
 */
public abstract class Habitacion {

    private final String codigoHabitacion;
    private final int numero;
    private int capacidad;
    private double precioNoche;
    private EstadoHabitacion estado;
    private String comodidades;

    public Habitacion(String codigoHabitacion, int numero, int capacidad,
                      double precioNoche, EstadoHabitacion estado, String comodidades) {
        this.codigoHabitacion = SecurityAndValidationUtil.validarCodigo(codigoHabitacion, "Habitación");
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de habitación debe ser un entero positivo.");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de la habitación debe ser un entero positivo.");
        }
        if (precioNoche <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser numérico y mayor que cero.");
        }
        this.numero = numero;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.estado = (estado != null) ? estado : EstadoHabitacion.DISPONIBLE;
        this.comodidades = (comodidades != null && !comodidades.trim().isEmpty())
                ? comodidades.trim()
                : getComodidadesPorDefecto();
    }

    public abstract String getTipo();

    public abstract double getCargoExtraNoche();

    public abstract String getComodidadesPorDefecto();

    /**
     * Calcula el costo total de la estancia de forma polimórfica según la categoría de habitación.
     * La fórmula base es: subtotal = noches * precioNoche, más recargos de categoría/temporada documentados.
     */
    public abstract double calcularCosto(int noches, boolean temporadaAlta);

    /**
     * Calcula el subtotal base (noches * precioNoche) conforme al Proceso 5.3.
     */
    public double calcularSubtotalBase(int noches) {
        if (noches <= 0) {
            throw new IllegalArgumentException("La cantidad de noches debe ser un entero positivo.");
        }
        return Math.round((this.precioNoche * noches) * 100.0) / 100.0;
    }

    /**
     * Calcula los cargos adicionales (temporada alta + servicios de categoría) para desglose en reportes.
     */
    public double calcularCargosAdicionales(int noches, boolean temporadaAlta) {
        double total = calcularCosto(noches, temporadaAlta);
        double base = calcularSubtotalBase(noches);
        return Math.round((total - base) * 100.0) / 100.0;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public int getNumero() {
        return numero;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un entero positivo.");
        }
        this.capacidad = capacidad;
    }

    public double getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(double precioNoche) {
        if (precioNoche <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser numérico y mayor que cero.");
        }
        this.precioNoche = precioNoche;
    }

    public EstadoHabitacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoHabitacion estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la habitación no puede ser nulo.");
        }
        this.estado = estado;
    }

    public String getComodidades() {
        return comodidades;
    }

    public void setComodidades(String comodidades) {
        if (comodidades != null && !comodidades.trim().isEmpty()) {
            this.comodidades = comodidades.trim();
        }
    }

    public String toJson() {
        return String.format(Locale.US,
            "{\"codigoHabitacion\":\"%s\",\"numero\":%d,\"tipo\":\"%s\",\"capacidad\":%d,\"precioNoche\":%.2f,\"estado\":\"%s\",\"comodidades\":\"%s\",\"cargoExtra\":%.2f}",
            SecurityAndValidationUtil.escaparJson(codigoHabitacion),
            numero,
            SecurityAndValidationUtil.escaparJson(getTipo()),
            capacidad,
            precioNoche,
            estado.name(),
            SecurityAndValidationUtil.escaparJson(comodidades),
            getCargoExtraNoche()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habitacion that = (Habitacion) o;
        return Objects.equals(codigoHabitacion, that.codigoHabitacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoHabitacion);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%s] Hab. #%d (%s) | Cap: %d pers. | $%.2f/noche | %s | %s",
                codigoHabitacion, numero, getTipo(), capacidad, precioNoche, estado.name(), comodidades);
    }
}
