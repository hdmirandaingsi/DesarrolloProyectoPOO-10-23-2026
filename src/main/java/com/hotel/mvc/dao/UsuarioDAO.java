package com.hotel.mvc.dao;

import com.hotel.mvc.model.Usuario;
import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión y autenticación de Usuarios en MySQL (RF-01 Login).
 */
public class UsuarioDAO {

    private final ConexionMySQL db;

    public UsuarioDAO() {
        this.db = ConexionMySQL.getInstance();
    }

    /**
     * Busca un usuario por nombre de usuario o correo electrónico.
     */
    public Optional<Usuario> buscarPorCredencial(String usuarioOCorreo) {
        if (usuarioOCorreo == null || usuarioOCorreo.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT id_usuario, username, email, nombre_completo, password_hash, salt, rol, activo "
                   + "FROM usuarios WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String criterio = usuarioOCorreo.trim();
            ps.setString(1, criterio);
            ps.setString(2, criterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error de lectura en MySQL al consultar usuario: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, username, email, nombre_completo, password_hash, salt, rol, activo FROM usuarios ORDER BY id_usuario";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar usuarios desde MySQL: " + e.getMessage(), e);
        }
        return lista;
    }

    public boolean registrarUsuario(String idUsuario, String username, String email,
                                    String nombreCompleto, String passwordPlano, String rol) {
        SecurityAndValidationUtil.validarTextoObligatorio(passwordPlano, "Contraseña");
        if (passwordPlano.trim().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        String salt = SecurityAndValidationUtil.generarSalt();
        String hash = SecurityAndValidationUtil.hashPassword(salt, passwordPlano);
        Usuario nuevo = new Usuario(idUsuario, username, email, nombreCompleto, hash, salt, rol, true);

        String sql = "INSERT INTO usuarios (id_usuario, username, email, nombre_completo, password_hash, salt, rol, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevo.getIdUsuario());
            ps.setString(2, nuevo.getUsername());
            ps.setString(3, nuevo.getEmail());
            ps.setString(4, nuevo.getNombreCompleto());
            ps.setString(5, nuevo.getPasswordHash());
            ps.setString(6, nuevo.getSalt());
            ps.setString(7, nuevo.getRol());
            ps.setBoolean(8, nuevo.isActivo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el usuario en MySQL (verifique duplicidad de código/usuario/correo): " + e.getMessage(), e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getString("id_usuario"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("nombre_completo"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getString("rol"),
            rs.getBoolean("activo")
        );
    }
}
