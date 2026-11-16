package modelo;

import java.time.LocalDate;
import java.util.List;

/** Contrato que debe implementar el DAO de reservas (parte del Modelo). */
public interface IReservaDAO {
    boolean insertar(Reserva r);
    List<Reserva> listar();
    boolean eliminar(int id);

    /**
     * true si la habitación ya tiene una reserva que se cruza con el rango dado.
     * SQL sugerido: WHERE num_habitacion = ? AND fecha_entrada < ? AND fecha_salida > ?
     * (parámetros: numHabitacion, salida, entrada)
     */
    boolean existeTraslape(int numHabitacion, LocalDate entrada, LocalDate salida);
}
