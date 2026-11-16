package modelo;

import java.util.List;

/** Contrato que debe implementar el DAO de clientes (parte del Modelo). */
public interface IClienteDAO {
    boolean insertar(Cliente c);
    Cliente buscarPorId(int id);              // null si no existe
    Cliente buscarPorDui(String dui);         // null si no existe
    List<Cliente> listar();
    boolean actualizar(Cliente c);
    boolean eliminar(int id);
}
