package com.hotel.mvc.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Configuración JDBC compartida para MySQL.
 * La conexión solo usa credenciales configuradas por el usuario; no intenta
 * credenciales alternativas ni crea o modifica el esquema automáticamente.
 */
public class ConexionMySQL {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static ConexionMySQL instance;

    private String host = "localhost";
    private int port = 3306;
    private String dbName = "hotel_mvc_db";
    private String user;
    private String pass;

    private ConexionMySQL() {
        cargarConfiguracion();
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró MySQL Connector/J en el classpath.", e);
        }
    }

    public static synchronized ConexionMySQL getInstance() {
        if (instance == null) {
            instance = new ConexionMySQL();
        }
        return instance;
    }

    private void cargarConfiguracion() {
        Properties props = new Properties();
        File propertiesFile = new File("db.properties");
        if (propertiesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                props.load(fis);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer db.properties.", e);
            }
        }

        host = valorConfigurado("hotel.db.host", "HOTEL_DB_HOST", props, "db.host", "localhost");
        String portValue = valorConfigurado("hotel.db.port", "HOTEL_DB_PORT", props, "db.port", "3306");
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La propiedad de puerto de MySQL debe ser un entero.", e);
        }
        dbName = valorConfigurado("hotel.db.name", "HOTEL_DB_NAME", props, "db.name", "hotel_mvc_db");
        user = valorConfigurado("hotel.db.user", "HOTEL_DB_USER", props, "db.user", "");
        pass = valorConfigurado("hotel.db.pass", "HOTEL_DB_PASS", props, "db.password", "");
    }

    private String valorConfigurado(String systemProperty, String environmentVariable,
                                    Properties props, String property, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(environmentVariable);
        }
        if (value == null || value.trim().isEmpty()) {
            value = props.getProperty(property, defaultValue);
        }
        return value.trim();
    }

    public String getDbUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
    }

    public Connection getConnection() throws SQLException {
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            throw new IllegalStateException(
                    "Configure HOTEL_DB_USER y HOTEL_DB_PASS, las propiedades Java equivalentes "
                    + "o los campos correspondientes en db.properties.");
        }
        return DriverManager.getConnection(getDbUrl(), user, pass);
    }

    public String getUser() {
        return user;
    }

    public String getDbName() {
        return dbName;
    }
}
