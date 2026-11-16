package controlador;

import java.util.List;
import modelo.Habitacion;
import modelo.IHabitacionDAO;

/**
 * Controlador de habitaciones.
 * Los métodos que guardan o modifican devuelven "OK" si todo salió bien,
 * o un mensaje de error que la Vista puede mostrar tal cual al usuario.
 */
public class HabitacionControlador {

    public static final String OK = "OK";
    public static final List<String> ESTADOS = List.of("Disponible", "Ocupada", "Mantenimiento");

    private final IHabitacionDAO dao;

    public HabitacionControlador(IHabitacionDAO dao) {
        this.dao = dao;
    }

    public String guardar(String numero, String tipo, String precio) {
        String error = validar(numero, tipo, precio);
        if (error != null) return error;

        int num = Integer.parseInt(numero.trim());
        if (dao.buscarPorNumero(num) != null) {
            return "Ya existe una habitación con ese número";
        }
        boolean ok = dao.insertar(new Habitacion(num, tipo.trim(), parsePrecio(precio)));
        return ok ? OK : "No se pudo guardar en la base de datos";
    }

    public String actualizar(String numero, String tipo, String precio, String estado) {
        String error = validar(numero, tipo, precio);
        if (error != null) return error;
        if (estado == null || !ESTADOS.contains(estado)) {
            return "Estado inválido (Disponible, Ocupada o Mantenimiento)";
        }
        int num = Integer.parseInt(numero.trim());
        if (dao.buscarPorNumero(num) == null) {
            return "La habitación no existe";
        }
        boolean ok = dao.actualizar(new Habitacion(num, tipo.trim(), parsePrecio(precio), estado));
        return ok ? OK : "No se pudo actualizar en la base de datos";
    }

    public String eliminar(int numero) {
        if (dao.buscarPorNumero(numero) == null) {
            return "La habitación no existe";
        }
        return dao.eliminar(numero) ? OK : "No se pudo eliminar (puede tener reservas asociadas)";
    }

    public Habitacion buscar(int numero) {
        return dao.buscarPorNumero(numero);
    }

    public List<Habitacion> listar() {
        return dao.listar();
    }

    //validaciones privadas

    private String validar(String numero, String tipo, String precio) {
        if (numero == null || numero.isBlank() || tipo == null || tipo.isBlank()
                || precio == null || precio.isBlank()) {
            return "Todos los campos son obligatorios";
        }
        int num;
        double prec;
        try {
            num = Integer.parseInt(numero.trim());
            prec = parsePrecio(precio);
        } catch (NumberFormatException e) {
            return "Número y precio deben ser numéricos";
        }
        if (num <= 0) return "El número de habitación debe ser mayor que cero";
        if (prec <= 0) return "El precio debe ser mayor que cero";
        if (tipo.trim().length() > 30) return "El tipo no puede pasar de 30 caracteres";
        return null;   // null = sin errores
    }

    private double parsePrecio(String precio) {
        return Double.parseDouble(precio.trim().replace(',', '.'));
    }
}
