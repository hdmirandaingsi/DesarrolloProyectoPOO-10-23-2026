package modelo;

/** Contrato que debe implementar el DAO de usuarios (parte del Modelo). */
public interface IUsuarioDAO {
    /**
     * true si existe el usuario con esa clave.
     * SQL sugerido: SELECT id FROM usuarios WHERE usuario = ? AND clave = SHA2(?, 256)
     */
    boolean validar(String usuario, String clave);
}
