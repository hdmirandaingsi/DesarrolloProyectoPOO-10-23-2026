package com.hotel.mvc.model;

import com.hotel.mvc.util.SecurityAndValidationUtil;
import java.util.Objects;

/**
 * Entidad de dominio Cliente (Proceso 1: Gestión de Clientes).
 * Aplica encapsulamiento estricto y validación de invariantes en el constructor y mutadores.
 */
public class Cliente {

    private final String codigoCliente;
    private String nombreCompleto;
    private String documento;
    private String telefono;
    private String email;
    private boolean activo;

    public Cliente(String codigoCliente, String nombreCompleto, String documento,
                   String telefono, String email, boolean activo) {
        this.codigoCliente = SecurityAndValidationUtil.validarCodigo(codigoCliente, "Cliente");
        this.nombreCompleto = SecurityAndValidationUtil.validarTextoObligatorio(nombreCompleto, "Nombre completo del cliente");
        this.documento = SecurityAndValidationUtil.validarTextoObligatorio(documento, "Documento de identificación");
        this.telefono = SecurityAndValidationUtil.validarTelefono(telefono);
        this.email = SecurityAndValidationUtil.validarEmail(email);
        this.activo = activo;
    }

    public Cliente(String codigoCliente, String nombreCompleto, String documento, String telefono, String email) {
        this(codigoCliente, nombreCompleto, documento, telefono, email, true);
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = SecurityAndValidationUtil.validarTextoObligatorio(nombreCompleto, "Nombre completo del cliente");
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = SecurityAndValidationUtil.validarTextoObligatorio(documento, "Documento de identificación");
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = SecurityAndValidationUtil.validarTelefono(telefono);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = SecurityAndValidationUtil.validarEmail(email);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String toJson() {
        return String.format(
            "{\"codigoCliente\":\"%s\",\"nombreCompleto\":\"%s\",\"documento\":\"%s\",\"telefono\":\"%s\",\"email\":\"%s\",\"activo\":%b}",
            SecurityAndValidationUtil.escaparJson(codigoCliente),
            SecurityAndValidationUtil.escaparJson(nombreCompleto),
            SecurityAndValidationUtil.escaparJson(documento),
            SecurityAndValidationUtil.escaparJson(telefono),
            SecurityAndValidationUtil.escaparJson(email),
            activo
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(codigoCliente, cliente.codigoCliente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoCliente);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Doc: %s | Tel: %s | Email: %s | %s",
                codigoCliente, nombreCompleto, documento, telefono, email,
                activo ? "ACTIVO" : "INACTIVO");
    }
}
