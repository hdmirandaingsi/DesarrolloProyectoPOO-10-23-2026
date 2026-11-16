package pruebas;

import controlador.ClienteControlador;
import controlador.HabitacionControlador;
import controlador.LoginControlador;
import controlador.ReservaControlador;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Habitacion;
import modelo.IClienteDAO;
import modelo.IHabitacionDAO;
import modelo.IReservaDAO;
import modelo.IUsuarioDAO;
import modelo.Reserva;

/**
 * Prueba los controladores SIN base de datos, usando DAO falsos en memoria.
 * Sirve para comprobar las validaciones antes de que el Modelo real esté listo.
 */
public class PruebaControladores {

    static int fallos = 0;

    static void esperar(String descripcion, String obtenido, String esperado) {
        boolean bien = obtenido.equals(esperado);
        if (!bien) fallos++;
        System.out.println((bien ? "[OK]    " : "[FALLA] ") + descripcion
                + (bien ? "" : "  -> obtuvo: " + obtenido));
    }

    public static void main(String[] args) {
        HabitacionDAOFalso habDao = new HabitacionDAOFalso();
        ClienteDAOFalso cliDao = new ClienteDAOFalso();
        ReservaDAOFalso resDao = new ReservaDAOFalso();

        HabitacionControlador hab = new HabitacionControlador(habDao);
        ClienteControlador cli = new ClienteControlador(cliDao);
        ReservaControlador res = new ReservaControlador(resDao, cliDao, habDao);
        LoginControlador login = new LoginControlador(
                (u, c) -> u.equals("admin") && c.equals("admin123"));

        System.out.println("--- Habitaciones ---");
        esperar("campos vacíos", hab.guardar("", "Doble", "50"), "Todos los campos son obligatorios");
        esperar("número no numérico", hab.guardar("abc", "Doble", "50"), "Número y precio deben ser numéricos");
        esperar("precio negativo", hab.guardar("101", "Doble", "-5"), "El precio debe ser mayor que cero");
        esperar("guardar válida", hab.guardar("101", "Doble", "55.50"), "OK");
        esperar("número repetido", hab.guardar("101", "Sencilla", "30"), "Ya existe una habitación con ese número");
        esperar("estado inválido", hab.actualizar("101", "Doble", "55", "Roto"),
                "Estado inválido (Disponible, Ocupada o Mantenimiento)");
        esperar("actualizar válida", hab.actualizar("101", "Doble", "60", "Disponible"), "OK");
        esperar("eliminar inexistente", hab.eliminar(999), "La habitación no existe");

        System.out.println("--- Clientes ---");
        esperar("DUI mal formado", cli.guardar("Ana Martínez", "1234", "7777-1111"),
                "El DUI debe tener el formato 00000000-0");
        esperar("nombre con números", cli.guardar("Ana123", "01234567-8", ""),
                "El nombre solo puede tener letras y espacios (mínimo 3 caracteres)");
        esperar("teléfono inválido", cli.guardar("Ana Martínez", "01234567-8", "12"),
                "El teléfono debe tener 8 dígitos (ej. 7777-1111)");
        esperar("guardar válido", cli.guardar("Ana Martínez", "01234567-8", "7777-1111"), "OK");
        esperar("DUI repetido", cli.guardar("Otra Persona", "01234567-8", ""), "Ya existe un cliente con ese DUI");

        System.out.println("--- Reservas ---");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String hoy = LocalDate.now().format(f);
        String en3 = LocalDate.now().plusDays(3).format(f);
        String en5 = LocalDate.now().plusDays(5).format(f);
        String ayer = LocalDate.now().minusDays(1).format(f);

        esperar("fecha con mal formato", res.guardar("1", "101", "2026-12-01", en5),
                "Las fechas deben tener el formato dd/MM/yyyy");
        esperar("entrada en el pasado", res.guardar("1", "101", ayer, en5),
                "La fecha de entrada no puede ser anterior a hoy");
        esperar("salida antes de entrada", res.guardar("1", "101", en5, en3),
                "La fecha de salida debe ser posterior a la de entrada");
        esperar("cliente inexistente", res.guardar("99", "101", en3, en5), "El cliente no existe");
        esperar("habitación inexistente", res.guardar("1", "555", en3, en5), "La habitación no existe");
        esperar("reserva válida", res.guardar("1", "101", en3, en5), "OK");
        esperar("traslape de fechas", res.guardar("1", "101", hoy.equals(en3) ? en5 : en3, en5),
                "La habitación ya está reservada en esas fechas");
        esperar("total (2 noches x 60)", String.valueOf(res.calcularTotal(res.listar().get(0))), "120.0");

        System.out.println("--- Login ---");
        esperar("credenciales correctas", String.valueOf(login.iniciarSesion("admin", "admin123")), "true");
        esperar("credenciales incorrectas", String.valueOf(login.iniciarSesion("admin", "mal")), "false");
        esperar("campos vacíos", String.valueOf(login.iniciarSesion("", "")), "false");

        System.out.println(fallos == 0 ? "\nTodas las pruebas pasaron." : "\nPruebas fallidas: " + fallos);
    }

    //DAO falsos en memoria 

    static class HabitacionDAOFalso implements IHabitacionDAO {
        final List<Habitacion> datos = new ArrayList<>();
        public boolean insertar(Habitacion h) { return datos.add(h); }
        public Habitacion buscarPorNumero(int n) {
            return datos.stream().filter(h -> h.getNumero() == n).findFirst().orElse(null);
        }
        public List<Habitacion> listar() { return datos; }
        public boolean actualizar(Habitacion h) {
            datos.removeIf(x -> x.getNumero() == h.getNumero());
            return datos.add(h);
        }
        public boolean eliminar(int n) { return datos.removeIf(h -> h.getNumero() == n); }
    }

    static class ClienteDAOFalso implements IClienteDAO {
        final List<Cliente> datos = new ArrayList<>();
        int siguienteId = 1;
        public boolean insertar(Cliente c) { c.setId(siguienteId++); return datos.add(c); }
        public Cliente buscarPorId(int id) {
            return datos.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        }
        public Cliente buscarPorDui(String dui) {
            return datos.stream().filter(c -> c.getDui().equals(dui)).findFirst().orElse(null);
        }
        public List<Cliente> listar() { return datos; }
        public boolean actualizar(Cliente c) {
            datos.removeIf(x -> x.getId() == c.getId());
            return datos.add(c);
        }
        public boolean eliminar(int id) { return datos.removeIf(c -> c.getId() == id); }
    }

    static class ReservaDAOFalso implements IReservaDAO {
        final List<Reserva> datos = new ArrayList<>();
        int siguienteId = 1;
        public boolean insertar(Reserva r) { r.setId(siguienteId++); return datos.add(r); }
        public List<Reserva> listar() { return datos; }
        public boolean eliminar(int id) { return datos.removeIf(r -> r.getId() == id); }
        public boolean existeTraslape(int numHab, LocalDate entrada, LocalDate salida) {
            return datos.stream().anyMatch(r -> r.getNumHabitacion() == numHab
                    && r.getFechaEntrada().isBefore(salida)
                    && r.getFechaSalida().isAfter(entrada));
        }
    }
}
