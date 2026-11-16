package com.hotel.mvc.dao;

import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.HabitacionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para el Proceso 2: Gestión de Habitaciones en MySQL.
 * Mapea polimórficamente las filas relacionales a instancias de HabitacionSimple, HabitacionDoble y HabitacionSuite.
 */
public class HabitacionDAO {

    private final ConexionMySQL db;

    public HabitacionDAO() {
        this.db = ConexionMySQL.getInstance();
    }

    public boolean insertar(Habitacion habitacion) {
        if (buscarPorCodigo(habitacion.getCodigoHabitacion()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el código: " + habitacion.getCodigoHabitacion());
        }
        if (buscarPorNumero(habitacion.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el número: " + habitacion.getNumero());
        }
        String sql = "INSERT INTO habitaciones (codigo_habitacion, numero, tipo, capacidad, precio_noche, estado, comodidades, cargo_extra) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, habitacion.getCodigoHabitacion());
            ps.setInt(2, habitacion.getNumero());
            ps.setString(3, habitacion.getTipo());
            ps.setInt(4, habitacion.getCapacidad());
            ps.setDouble(5, habitacion.getPrecioNoche());
            ps.setString(6, habitacion.getEstado().name());
            ps.setString(7, habitacion.getComodidades());
            ps.setDouble(8, habitacion.getCargoExtraNoche());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al registrar habitación: " + e.getMessage(), e);
        }
    }

    public boolean actualizar(Habitacion habitacion) {
        if (!buscarPorCodigo(habitacion.getCodigoHabitacion()).isPresent()) {
            throw new IllegalArgumentException("No existe la habitación con código: " + habitacion.getCodigoHabitacion());
        }
        Optional<Habitacion> porNum = buscarPorNumero(habitacion.getNumero());
        if (porNum.isPresent() && !porNum.get().getCodigoHabitacion().equalsIgnoreCase(habitacion.getCodigoHabitacion())) {
            throw new IllegalArgumentException("El número de habitación " + habitacion.getNumero() + " ya está asignado a otra habitación.");
        }
        String sql = "UPDATE habitaciones SET numero = ?, tipo = ?, capacidad = ?, precio_noche = ?, estado = ?, comodidades = ?, cargo_extra = ? "
                   + "WHERE codigo_habitacion = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, habitacion.getNumero());
            ps.setString(2, habitacion.getTipo());
            ps.setInt(3, habitacion.getCapacidad());
            ps.setDouble(4, habitacion.getPrecioNoche());
            ps.setString(5, habitacion.getEstado().name());
            ps.setString(6, habitacion.getComodidades());
            ps.setDouble(7, habitacion.getCargoExtraNoche());
            ps.setString(8, habitacion.getCodigoHabitacion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al actualizar habitación: " + e.getMessage(), e);
        }
    }

    public boolean cambiarEstado(String codigoHabitacion, EstadoHabitacion nuevoEstado) {
        if (!buscarPorCodigo(codigoHabitacion).isPresent()) {
            throw new IllegalArgumentException("No existe la habitación con código: " + codigoHabitacion);
        }
        String sql = "UPDATE habitaciones SET estado = ? WHERE codigo_habitacion = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setString(2, codigoHabitacion.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al cambiar estado de habitación: " + e.getMessage(), e);
        }
    }

    /**
     * Proceso 2.6: Eliminar habitación únicamente si no existen reservas asociadas.
     */
    public boolean eliminar(String codigoHabitacion) {
        if (!buscarPorCodigo(codigoHabitacion).isPresent()) {
            throw new IllegalArgumentException("No existe la habitación con código: " + codigoHabitacion);
        }
        int totalReservas = contarReservasAsociadas(codigoHabitacion);
        if (totalReservas > 0) {
            throw new IllegalStateException("No se puede eliminar la habitación '" + codigoHabitacion
                    + "' porque tiene " + totalReservas + " reserva(s) histórica(s) asociada(s).");
        }
        String sql = "DELETE FROM habitaciones WHERE codigo_habitacion = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoHabitacion.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al eliminar habitación: " + e.getMessage(), e);
        }
    }

    public int contarReservasAsociadas(String codigoHabitacion) {
        String sql = "SELECT COUNT(*) FROM reservas WHERE codigo_habitacion = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoHabitacion.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al verificar dependencias de la habitación: " + e.getMessage(), e);
        }
        return 0;
    }

    public Optional<Habitacion> buscarPorCodigo(String codigoHabitacion) {
        if (codigoHabitacion == null || codigoHabitacion.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT codigo_habitacion, numero, tipo, capacidad, precio_noche, estado, comodidades FROM habitaciones WHERE codigo_habitacion = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoHabitacion.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearHabitacion(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar habitación por código: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Habitacion> buscarPorNumero(int numero) {
        String sql = "SELECT codigo_habitacion, numero, tipo, capacidad, precio_noche, estado, comodidades FROM habitaciones WHERE numero = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearHabitacion(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar habitación por número: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Proceso 2.3: Buscar habitación por código, número o tipo.
     */
    public List<Habitacion> buscarPorCriterio(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return listarTodas();
        }
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT codigo_habitacion, numero, tipo, capacidad, precio_noche, estado, comodidades FROM habitaciones "
                   + "WHERE UPPER(codigo_habitacion) LIKE UPPER(?) OR CAST(numero AS CHAR) LIKE ? OR UPPER(tipo) LIKE UPPER(?) "
                   + "ORDER BY numero";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + criterio.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearHabitacion(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar habitaciones: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Habitacion> listarTodas() {
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT codigo_habitacion, numero, tipo, capacidad, precio_noche, estado, comodidades FROM habitaciones ORDER BY numero";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearHabitacion(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al listar habitaciones: " + e.getMessage(), e);
        }
        return lista;
    }

    public static Habitacion mapearHabitacion(ResultSet rs) throws SQLException {
        return HabitacionFactory.crearHabitacion(
            rs.getString("tipo"),
            rs.getString("codigo_habitacion"),
            rs.getInt("numero"),
            rs.getInt("capacidad"),
            rs.getDouble("precio_noche"),
            EstadoHabitacion.valueOf(rs.getString("estado")),
            rs.getString("comodidades")
        );
    }
}
