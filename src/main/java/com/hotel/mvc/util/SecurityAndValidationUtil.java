package com.hotel.mvc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Utilidad de seguridad y validación de datos de entrada (RF-01 y RF-04).
 * Implementa hash criptográfico SHA-256 con Salt para contraseñas y validaciones de formato.
 */
public final class SecurityAndValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CODIGO_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[0-9+()\\s-]{7,20}$");

    private SecurityAndValidationUtil() {
        // Clase utilitaria estática
    }

    /**
     * Genera un salt aleatorio hexadecimal de 16 caracteres.
     */
    public static String generarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Calcula el hash SHA-256 en formato hexadecimal minúscula para (salt + ":" + password).
     * Equivalente a SHA2(CONCAT(salt, ':', password), 256) en MySQL 8.0.
     */
    public static String hashPassword(String salt, String rawPassword) {
        if (salt == null || rawPassword == null) {
            throw new IllegalArgumentException("El salt y la contraseña no pueden ser nulos.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = salt + ":" + rawPassword;
            byte[] encoded = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encoded.length);
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible en el entorno Java.", e);
        }
    }

    /**
     * Verifica si una contraseña en texto plano coincide con el hash y salt almacenados.
     */
    public static boolean verificarPassword(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String computed = hashPassword(salt, rawPassword);
        return computed.equalsIgnoreCase(expectedHash.trim());
    }

    public static String validarTextoObligatorio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' es obligatorio y no puede estar vacío.");
        }
        return valor.trim();
    }

    public static String validarCodigo(String codigo, String entidad) {
        String limpio = validarTextoObligatorio(codigo, "Código de " + entidad).toUpperCase();
        if (!CODIGO_PATTERN.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El código de " + entidad + " ('" + limpio + "') debe tener entre 3 y 20 caracteres alfanuméricos o guiones.");
        }
        return limpio;
    }

    public static String validarEmail(String email) {
        String limpio = validarTextoObligatorio(email, "Correo electrónico");
        if (!EMAIL_PATTERN.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico '" + limpio + "' no es válido (ejemplo: usuario@dominio.com).");
        }
        return limpio;
    }

    public static String validarTelefono(String telefono) {
        String limpio = validarTextoObligatorio(telefono, "Teléfono");
        if (!TELEFONO_PATTERN.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El teléfono '" + limpio + "' contiene un formato inválido (mínimo 7 dígitos).");
        }
        return limpio;
    }

    public static String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
