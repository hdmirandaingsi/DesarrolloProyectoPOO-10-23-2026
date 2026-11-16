package controlador;

import java.util.List;
import modelo.Cliente;
import modelo.IClienteDAO;

/**
 * Controlador de clientes.
 * Devuelve "OK" o un mensaje de error listo para mostrar en la Vista
 */
public class ClienteControlador {

    public static final String OK = "OK";

    private final IClienteDAO dao;

    public ClienteControlador(IClienteDAO dao) {
        this.dao = dao;
    }

    public String guardar(String nombre, String dui, String telefono) {
        String error = validar(nombre, dui, telefono);
        if (error != null) return error;

        if (dao.buscarPorDui(dui.trim()) != null) {
            return "Ya existe un cliente con ese DUI";
        }
        boolean ok = dao.insertar(new Cliente(nombre.trim(), dui.trim(), limpiar(telefono)));
        return ok ? OK : "No se pudo guardar en la base de datos";
    }

    public String actualizar(int id, String nombre, String dui, String telefono) {
        String error = validar(nombre, dui, telefono);
        if (error != null) return error;

        if (dao.buscarPorId(id) == null) {
            return "El cliente no existe";
        }
        Cliente otro = dao.buscarPorDui(dui.trim());
        if (otro != null && otro.getId() != id) {
            return "Otro cliente ya tiene ese DUI";
        }
        boolean ok = dao.actualizar(new Cliente(id, nombre.trim(), dui.trim(), limpiar(telefono)));
        return ok ? OK : "No se pudo actualizar en la base de datos";
    }

    public String eliminar(int id) {
        if (dao.buscarPorId(id) == null) {
            return "El cliente no existe";
        }
        return dao.eliminar(id) ? OK : "No se pudo eliminar (puede tener reservas asociadas)";
    }

    public Cliente buscarPorDui(String dui) {
        return dui == null ? null : dao.buscarPorDui(dui.trim());
    }

    public List<Cliente> listar() {
        return dao.listar();
    }

    //validaciones privadas

    private String validar(String nombre, String dui, String telefono) {
        if (nombre == null || nombre.isBlank() || dui == null || dui.isBlank()) {
            return "Nombre y DUI son obligatorios";
        }
        if (!nombre.trim().matches("[\\p{L} .'-]{3,80}")) {
            return "El nombre solo puede tener letras y espacios (mínimo 3 caracteres)";
        }
        if (!dui.trim().matches("\\d{8}-\\d")) {
            return "El DUI debe tener el formato 00000000-0";
        }
        if (telefono != null && !telefono.isBlank() && !telefono.trim().matches("\\d{4}-?\\d{4}")) {
            return "El teléfono debe tener 8 dígitos (ej. 7777-1111)";
        }
        return null;   // null = sin errores
    }

    private String limpiar(String telefono) {
        return telefono == null ? "" : telefono.trim();
    }
}
