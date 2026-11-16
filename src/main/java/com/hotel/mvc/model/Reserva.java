package com.hotel.mvc.model;

import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

/**
 * Entidad de dominio Reserva (Procesos 3, 4 y 5: Disponibilidad, Gestión de Reservas y Cálculo de Costo).
 * Aplica composición con Cliente y Habitacion, validación de capacidad y fechas,
 * detección de superposición de períodos y cálculo polimórfico del costo total.
 */
public class Reserva {

    private final String codigoReserva;
    private final Cliente cliente;
    private Habitacion habitacion;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private int cantidadHuespedes;
    private boolean temporadaAlta;
    private int noches;
    private double tarifaBaseNoche;
    private double subtotalBase;
    private double cargosAdicionales;
    private double costoTotal;
    private EstadoReserva estado;

    /**
     * Constructor para nuevas reservas creadas desde la lógica de negocio.
     */
    public Reserva(String codigoReserva, Cliente cliente, Habitacion habitacion,
                   LocalDate fechaEntrada, LocalDate fechaSalida,
                   int cantidadHuespedes, boolean temporadaAlta) {
        this.codigoReserva = SecurityAndValidationUtil.validarCodigo(codigoReserva, "Reserva");
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente seleccionado no existe o es nulo.");
        }
        if (!cliente.isActivo()) {
            throw new IllegalArgumentException("El cliente '" + cliente.getCodigoCliente() + "' se encuentra inactivo.");
        }
        if (habitacion == null) {
            throw new IllegalArgumentException("La habitación seleccionada no existe o es nula.");
        }
        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("La habitación " + habitacion.getNumero() + " se encuentra FUERA DE SERVICIO.");
        }
        validarPeriodoYCapacidad(habitacion, fechaEntrada, fechaSalida, cantidadHuespedes);

        this.cliente = cliente;
        this.habitacion = habitacion;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.cantidadHuespedes = cantidadHuespedes;
        this.temporadaAlta = temporadaAlta;
        this.estado = EstadoReserva.CONFIRMADA;
        recalcularImportes();
    }

    /**
     * Constructor completo para reconstrucción desde la base de datos MySQL (DAO).
     */
    public Reserva(String codigoReserva, Cliente cliente, Habitacion habitacion,
                   LocalDate fechaEntrada, LocalDate fechaSalida, int cantidadHuespedes,
                   boolean temporadaAlta, int noches, double tarifaBaseNoche,
                   double subtotalBase, double cargosAdicionales, double costoTotal,
                   EstadoReserva estado) {
        this.codigoReserva = SecurityAndValidationUtil.validarCodigo(codigoReserva, "Reserva");
        this.cliente = Objects.requireNonNull(cliente, "Cliente requerido");
        this.habitacion = Objects.requireNonNull(habitacion, "Habitación requerida");
        this.fechaEntrada = Objects.requireNonNull(fechaEntrada, "Fecha de entrada requerida");
        this.fechaSalida = Objects.requireNonNull(fechaSalida, "Fecha de salida requerida");
        this.cantidadHuespedes = cantidadHuespedes;
        this.temporadaAlta = temporadaAlta;
        this.noches = noches;
        this.tarifaBaseNoche = tarifaBaseNoche;
        this.subtotalBase = subtotalBase;
        this.cargosAdicionales = cargosAdicionales;
        this.costoTotal = costoTotal;
        this.estado = (estado != null) ? estado : EstadoReserva.CONFIRMADA;
    }

    public static void validarPeriodo(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias.");
        }
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida (" + salida + ") debe ser posterior a la fecha de entrada (" + entrada + ").");
        }
    }

    private static void validarPeriodoYCapacidad(Habitacion hab, LocalDate entrada, LocalDate salida, int huespedes) {
        validarPeriodo(entrada, salida);
        if (huespedes <= 0) {
            throw new IllegalArgumentException("La cantidad de huéspedes debe ser un entero positivo.");
        }
        if (huespedes > hab.getCapacidad()) {
            throw new IllegalArgumentException("La cantidad de huéspedes (" + huespedes
                    + ") supera la capacidad máxima de la habitación #" + hab.getNumero()
                    + " (" + hab.getCapacidad() + " personas).");
        }
    }

    /**
     * Proceso 5: Cálculo del costo (Information Expert + Polimorfismo).
     * 5.1 Calcular noches entre entrada y salida.
     * 5.2 Obtener tarifa por noche de la habitación.
     * 5.3 Calcular subtotal base = noches * tarifa.
     * 5.4 Calcular costo total mediante despacho polimórfico habitacion.calcularCosto(noches, temporadaAlta).
     */
    public final void recalcularImportes() {
        this.noches = (int) ChronoUnit.DAYS.between(this.fechaEntrada, this.fechaSalida);
        if (this.noches <= 0) {
            throw new IllegalArgumentException("La estancia mínima debe ser de al menos 1 noche.");
        }
        this.tarifaBaseNoche = this.habitacion.getPrecioNoche();
        this.subtotalBase = this.habitacion.calcularSubtotalBase(this.noches);
        this.costoTotal = this.habitacion.calcularCosto(this.noches, this.temporadaAlta);
        this.cargosAdicionales = this.habitacion.calcularCargosAdicionales(this.noches, this.temporadaAlta);
    }

    /**
     * Proceso 3.3: Determina si esta reserva confirmada se superpone con el período [inicioSolicitado, salidaSolicitada).
     * Regla del documento:
     * "Se considerará que dos reservas se superponen cuando el inicio de una reserva sea anterior a la salida de la otra
     * y su salida sea posterior al inicio de la otra. Una reserva que termina el mismo día en que comienza otra no se
     * considerará superpuesta."
     */
    public boolean seSuperponeCon(LocalDate inicioSolicitado, LocalDate salidaSolicitada) {
        validarPeriodo(inicioSolicitado, salidaSolicitada);
        if (this.estado != EstadoReserva.CONFIRMADA) {
            return false;
        }
        return this.fechaEntrada.isBefore(salidaSolicitada) && this.fechaSalida.isAfter(inicioSolicitado);
    }

    /**
     * Proceso 4.6: Actualizar fechas, habitación, huéspedes o temporada de una reserva activa.
     */
    public void actualizarDatosReserva(Habitacion nuevaHabitacion, LocalDate nuevaEntrada,
                                       LocalDate nuevaSalida, int nuevosHuespedes, boolean nuevaTemporadaAlta) {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden modificar reservas en estado CONFIRMADA.");
        }
        if (nuevaHabitacion == null) {
            throw new IllegalArgumentException("La habitación seleccionada no existe.");
        }
        if (nuevaHabitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("La habitación " + nuevaHabitacion.getNumero() + " está FUERA DE SERVICIO.");
        }
        validarPeriodoYCapacidad(nuevaHabitacion, nuevaEntrada, nuevaSalida, nuevosHuespedes);
        this.habitacion = nuevaHabitacion;
        this.fechaEntrada = nuevaEntrada;
        this.fechaSalida = nuevaSalida;
        this.cantidadHuespedes = nuevosHuespedes;
        this.temporadaAlta = nuevaTemporadaAlta;
        recalcularImportes();
    }

    /**
     * Proceso 4.5: Cancelar reserva (corrige el hallazgo de la tarea de investigación:
     * solo permite cancelar desde el estado CONFIRMADA).
     */
    public void cancelar() {
        if (this.estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva " + codigoReserva + " ya se encuentra CANCELADA.");
        }
        if (this.estado == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar la reserva " + codigoReserva + " porque ya fue COMPLETADA.");
        }
        this.estado = EstadoReserva.CANCELADA;
    }

    public void completar() {
        if (this.estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo las reservas CONFIRMADAS pueden marcarse como COMPLETADAS.");
        }
        this.estado = EstadoReserva.COMPLETADA;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public int getCantidadHuespedes() {
        return cantidadHuespedes;
    }

    public boolean isTemporadaAlta() {
        return temporadaAlta;
    }

    public int getNoches() {
        return noches;
    }

    public double getTarifaBaseNoche() {
        return tarifaBaseNoche;
    }

    public double getSubtotalBase() {
        return subtotalBase;
    }

    public double getCargosAdicionales() {
        return cargosAdicionales;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public String toJson() {
        return String.format(Locale.US,
            "{\"codigoReserva\":\"%s\",\"codigoCliente\":\"%s\",\"nombreCliente\":\"%s\",\"codigoHabitacion\":\"%s\",\"numeroHabitacion\":%d,\"tipoHabitacion\":\"%s\",\"fechaEntrada\":\"%s\",\"fechaSalida\":\"%s\",\"cantidadHuespedes\":%d,\"temporadaAlta\":%b,\"noches\":%d,\"tarifaBaseNoche\":%.2f,\"subtotalBase\":%.2f,\"cargosAdicionales\":%.2f,\"costoTotal\":%.2f,\"estado\":\"%s\"}",
            SecurityAndValidationUtil.escaparJson(codigoReserva),
            SecurityAndValidationUtil.escaparJson(cliente.getCodigoCliente()),
            SecurityAndValidationUtil.escaparJson(cliente.getNombreCompleto()),
            SecurityAndValidationUtil.escaparJson(habitacion.getCodigoHabitacion()),
            habitacion.getNumero(),
            SecurityAndValidationUtil.escaparJson(habitacion.getTipo()),
            fechaEntrada.toString(),
            fechaSalida.toString(),
            cantidadHuespedes,
            temporadaAlta,
            noches,
            tarifaBaseNoche,
            subtotalBase,
            cargosAdicionales,
            costoTotal,
            estado.name()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(codigoReserva, reserva.codigoReserva);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoReserva);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "[%s] Cliente: %s (%s) | Hab: #%d (%s) | %s a %s (%d noches, %d huésp., TempAlta: %s) | Base: $%.2f + Extra: $%.2f = Total: $%.2f | %s",
            codigoReserva, cliente.getNombreCompleto(), cliente.getCodigoCliente(),
            habitacion.getNumero(), habitacion.getTipo(),
            fechaEntrada, fechaSalida, noches, cantidadHuespedes,
            temporadaAlta ? "SÍ" : "NO",
            subtotalBase, cargosAdicionales, costoTotal, estado.name()
        );
    }
}
