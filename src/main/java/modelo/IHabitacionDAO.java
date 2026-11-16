package modelo;

import java.util.List;

/** Contrato que debe implementar el DAO de habitaciones (parte del Modelo). */
public interface IHabitacionDAO {
    boolean insertar(Habitacion h);
    Habitacion buscarPorNumero(int numero);   // null si no existe
    List<Habitacion> listar();
    boolean actualizar(Habitacion h);
    boolean eliminar(int numero);
}
