package com.hotel.mvc.dao;

import com.hotel.mvc.model.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para el Proceso 1: Gestión de Clientes en MySQL.
 * Implementa registro, búsqueda por código/documento/nombre, listado, modificación y eliminación controlada.
 */
public class ClienteDAO {

    private final ConexionMySQL db;

    public ClienteDAO() {
        this.db = ConexionMySQL.getInstance();
    }

    public boolean insertar(Cliente cliente) {
        if (buscarPorCodigo(cliente.getCodigoCliente()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el código: " + cliente.getCodigoCliente());
        }
        if (existeDocumento(cliente.getDocumento(), null)) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el documento: " + cliente.getDocumento());
        }
        String sql = "INSERT INTO clientes (codigo_cliente, nombre_completo, documento, telefono, email, activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getCodigoCliente());
            ps.setString(2, cliente.getNombreCompleto());
            ps.setString(3, cliente.getDocumento());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getEmail());
            ps.setBoolean(6, cliente.isActivo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al registrar cliente: " + e.getMessage(), e);
        }
    }

    public boolean actualizar(Cliente cliente) {
        if (!buscarPorCodigo(cliente.getCodigoCliente()).isPresent()) {
            throw new IllegalArgumentException("No existe el cliente con código: " + cliente.getCodigoCliente());
        }
        if (existeDocumento(cliente.getDocumento(), cliente.getCodigoCliente())) {
            throw new IllegalArgumentException("El documento '" + cliente.getDocumento() + "' ya pertenece a otro cliente.");
        }
        String sql = "UPDATE clientes SET nombre_completo = ?, documento = ?, telefono = ?, email = ?, activo = ? WHERE codigo_cliente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombreCompleto());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getEmail());
            ps.setBoolean(5, cliente.isActivo());
            ps.setString(6, cliente.getCodigoCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al modificar cliente: " + e.getMessage(), e);
        }
    }

    /**
     * Proceso 1.5: Eliminar cliente únicamente cuando no existan reservas históricas asociadas.
     */
    public boolean eliminar(String codigoCliente) {
        if (!buscarPorCodigo(codigoCliente).isPresent()) {
            throw new IllegalArgumentException("No existe el cliente con código: " + codigoCliente);
        }
        int totalReservas = contarReservasAsociadas(codigoCliente);
        if (totalReservas > 0) {
            throw new IllegalStateException("No se puede eliminar el cliente '" + codigoCliente
                    + "' porque tiene " + totalReservas + " reserva(s) asociada(s) en el historial.");
        }
        String sql = "DELETE FROM clientes WHERE codigo_cliente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoCliente.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al eliminar cliente: " + e.getMessage(), e);
        }
    }

    public int contarReservasAsociadas(String codigoCliente) {
        String sql = "SELECT COUNT(*) FROM reservas WHERE codigo_cliente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoCliente.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al verificar dependencias del cliente: " + e.getMessage(), e);
        }
        return 0;
    }

    public Optional<Cliente> buscarPorCodigo(String codigoCliente) {
        if (codigoCliente == null || codigoCliente.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT codigo_cliente, nombre_completo, documento, telefono, email, activo FROM clientes WHERE codigo_cliente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoCliente.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar cliente por código: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Proceso 1.2: Buscar cliente por código, documento o nombre.
     */
    public List<Cliente> buscarPorCriterio(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return listarTodos();
        }
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT codigo_cliente, nombre_completo, documento, telefono, email, activo FROM clientes "
                   + "WHERE UPPER(codigo_cliente) LIKE UPPER(?) OR UPPER(documento) LIKE UPPER(?) OR UPPER(nombre_completo) LIKE UPPER(?) "
                   + "ORDER BY codigo_cliente";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + criterio.trim() + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT codigo_cliente, nombre_completo, documento, telefono, email, activo FROM clientes ORDER BY codigo_cliente";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al consultar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    private boolean existeDocumento(String documento, String excluirCodigo) {
        String sql = (excluirCodigo == null)
                ? "SELECT COUNT(*) FROM clientes WHERE UPPER(documento) = UPPER(?)"
                : "SELECT COUNT(*) FROM clientes WHERE UPPER(documento) = UPPER(?) AND codigo_cliente <> ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documento.trim());
            if (excluirCodigo != null) {
                ps.setString(2, excluirCodigo.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al validar documento único: " + e.getMessage(), e);
        }
        return false;
    }

    public static Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("codigo_cliente"),
            rs.getString("nombre_completo"),
            rs.getString("documento"),
            rs.getString("telefono"),
            rs.getString("email"),
            rs.getBoolean("activo")
        );
    }
}
