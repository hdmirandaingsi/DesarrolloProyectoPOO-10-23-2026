package controlador;

import modelo.IUsuarioDAO;

/** Controlador del inicio de sesión (Login obligatorio). */
public class LoginControlador {

    private final IUsuarioDAO dao;

    public LoginControlador(IUsuarioDAO dao) {
        this.dao = dao;
    }

    /** true si las credenciales son correctas; la Vista decide si abre el menú principal. */
    public boolean iniciarSesion(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return false;
        }
        return dao.validar(usuario.trim(), clave);
    }
}
