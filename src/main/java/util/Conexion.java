package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión JDBC a la base de datos del hotel.
 * Todos los DAO del modelo obtienen su conexión desde aquí.
 */
public class Conexion {

    // Usuario creado por sql/hotel.sql (el mismo en la PC de cada integrante)
    private static final String URL = "jdbc:mysql://localhost:3306/hotel?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/El_Salvador";
    private static final String USUARIO = "hotel_user";
    private static final String CLAVE = "hotel123";

    private Conexion() { }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /** Prueba rápida para verificar que la conexión funciona. */
    public static void main(String[] args) {
        try (Connection c = getConexion()) {
            System.out.println("Conexión exitosa");
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}
