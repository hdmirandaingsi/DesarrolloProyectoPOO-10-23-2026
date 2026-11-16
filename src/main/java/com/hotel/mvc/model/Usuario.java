package com.hotel.mvc.model;

import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.util.Objects;

/**
 * Entidad de dominio para el control de acceso (RF-01 Login y RF-06 Cierre de Sesión).
 * Nunca almacena la contraseña en texto plano, únicamente su hash SHA-256 y salt.
 */
public class Usuario {

    private final String idUsuario;
    private String username;
    private String email;
    private String nombreCompleto;
    private String passwordHash;
    private String salt;
    private String rol;
    private boolean activo;

    public Usuario(String idUsuario, String username, String email, String nombreCompleto,
                   String passwordHash, String salt, String rol, boolean activo) {
        this.idUsuario = SecurityAndValidationUtil.validarCodigo(idUsuario, "Usuario");
        this.username = SecurityAndValidationUtil.validarTextoObligatorio(username, "Nombre de usuario");
        this.email = SecurityAndValidationUtil.validarEmail(email);
        this.nombreCompleto = SecurityAndValidationUtil.validarTextoObligatorio(nombreCompleto, "Nombre completo");
        this.passwordHash = SecurityAndValidationUtil.validarTextoObligatorio(passwordHash, "Hash de contraseña");
        this.salt = SecurityAndValidationUtil.validarTextoObligatorio(salt, "Salt criptográfico");
        this.rol = (rol != null && !rol.trim().isEmpty()) ? rol.trim().toUpperCase() : "RECEPCIONISTA";
        this.activo = activo;
    }

    public boolean autenticar(String passwordPlano) {
        if (!activo) {
            return false;
        }
        return SecurityAndValidationUtil.verificarPassword(passwordPlano, this.salt, this.passwordHash);
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String toJson() {
        return String.format(
            "{\"idUsuario\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"nombreCompleto\":\"%s\",\"rol\":\"%s\",\"activo\":%b}",
            SecurityAndValidationUtil.escaparJson(idUsuario),
            SecurityAndValidationUtil.escaparJson(username),
            SecurityAndValidationUtil.escaparJson(email),
            SecurityAndValidationUtil.escaparJson(nombreCompleto),
            SecurityAndValidationUtil.escaparJson(rol),
            activo
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(idUsuario, usuario.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    @Override
    public String toString() {
        return String.format("Usuario[%s | %s (%s) - Rol: %s - Activo: %b]",
                idUsuario, username, nombreCompleto, rol, activo);
    }
}
