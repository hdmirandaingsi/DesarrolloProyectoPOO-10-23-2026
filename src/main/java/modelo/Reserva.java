package modelo;

import java.time.LocalDate;

public class Reserva {
    private int id;
    private int idCliente;
    private int numHabitacion;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    public Reserva(int idCliente, int numHabitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        this(0, idCliente, numHabitacion, fechaEntrada, fechaSalida);
    }

    public Reserva(int id, int idCliente, int numHabitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        this.id = id;
        this.idCliente = idCliente;
        this.numHabitacion = numHabitacion;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public int getNumHabitacion() { return numHabitacion; }
    public void setNumHabitacion(int numHabitacion) { this.numHabitacion = numHabitacion; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDate fechaSalida) { this.fechaSalida = fechaSalida; }
}
