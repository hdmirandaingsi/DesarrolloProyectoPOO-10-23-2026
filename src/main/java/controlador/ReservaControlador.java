package controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import modelo.Cliente;
import modelo.Habitacion;
import modelo.IClienteDAO;
import modelo.IHabitacionDAO;
import modelo.IReservaDAO;
import modelo.Reserva;

/**
 * Controlador de reservas.
 * Las fechas llegan como texto con el formato dd/MM/yyyy (20/11/2026).
 */
public class ReservaControlador {

    public static final String OK = "OK";
    public static final String FORMATO_FECHA = "dd/MM/yyyy";

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final IReservaDAO reservaDAO;
    private final IClienteDAO clienteDAO;
    private final IHabitacionDAO habitacionDAO;

    public ReservaControlador(IReservaDAO reservaDAO, IClienteDAO clienteDAO,
                              IHabitacionDAO habitacionDAO) {
        this.reservaDAO = reservaDAO;
        this.clienteDAO = clienteDAO;
        this.habitacionDAO = habitacionDAO;
    }

    public String guardar(String idCliente, String numHabitacion, String entrada, String salida) {
        if (isVacio(idCliente) || isVacio(numHabitacion) || isVacio(entrada) || isVacio(salida)) {
            return "Todos los campos son obligatorios";
        }

        int idCli;
        int numHab;
        try {
            idCli = Integer.parseInt(idCliente.trim());
            numHab = Integer.parseInt(numHabitacion.trim());
        } catch (NumberFormatException e) {
            return "El cliente y la habitación deben ser números";
        }

        LocalDate fEntrada;
        LocalDate fSalida;
        try {
            fEntrada = LocalDate.parse(entrada.trim(), FMT);
            fSalida = LocalDate.parse(salida.trim(), FMT);
        } catch (DateTimeParseException e) {
            return "Las fechas deben tener el formato " + FORMATO_FECHA;
        }

        if (fEntrada.isBefore(LocalDate.now())) {
            return "La fecha de entrada no puede ser anterior a hoy";
        }
        if (!fSalida.isAfter(fEntrada)) {
            return "La fecha de salida debe ser posterior a la de entrada";
        }

        Cliente cliente = clienteDAO.buscarPorId(idCli);
        if (cliente == null) {
            return "El cliente no existe";
        }
        Habitacion hab = habitacionDAO.buscarPorNumero(numHab);
        if (hab == null) {
            return "La habitación no existe";
        }
        if ("Mantenimiento".equals(hab.getEstado())) {
            return "La habitación está en mantenimiento";
        }
        if (reservaDAO.existeTraslape(numHab, fEntrada, fSalida)) {
            return "La habitación ya está reservada en esas fechas";
        }

        boolean ok = reservaDAO.insertar(new Reserva(idCli, numHab, fEntrada, fSalida));
        return ok ? OK : "No se pudo guardar en la base de datos";
    }

    public String cancelar(int idReserva) {
        return reservaDAO.eliminar(idReserva) ? OK : "No se pudo cancelar la reserva";
    }

    public List<Reserva> listar() {
        return reservaDAO.listar();
    }

    /** Total a pagar: noches x precio de la habitación. Devuelve -1 si algo no existe. */
    public double calcularTotal(Reserva r) {
        Habitacion hab = habitacionDAO.buscarPorNumero(r.getNumHabitacion());
        if (hab == null) return -1;
        long noches = java.time.temporal.ChronoUnit.DAYS.between(r.getFechaEntrada(), r.getFechaSalida());
        return noches * hab.getPrecio();
    }

    private boolean isVacio(String s) {
        return s == null || s.isBlank();
    }
}
