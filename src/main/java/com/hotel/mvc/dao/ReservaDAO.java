package com.hotel.mvc.dao;

import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.EstadoReserva;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.HabitacionFactory;
import com.hotel.mvc.model.Reserva;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para los Procesos 3 y 4: Consulta de Disponibilidad y Gestión de Reservas en MySQL.
 */
public class ReservaDAO {

    private final ConexionMySQL db;

    private static final String SELECT_JOIN_BASE =
        "SELECT r.codigo_reserva, r.fecha_entrada, r.fecha_salida, r.cantidad_huespedes, "
      + "r.temporada_alta, r.noches, r.tarifa_base_noche, r.subtotal_base, r.cargos_adicionales, r.costo_total, r.estado AS estado_reserva, "
      + "c.codigo_cliente, c.nombre_completo, c.documento, c.telefono, c.email, c.activo AS cliente_activo, "
      + "h.codigo_habitacion, h.numero, h.tipo, h.capacidad, h.precio_noche, h.estado AS estado_habitacion, h.comodidades "
      + "FROM reservas r "
      + "INNER JOIN clientes c ON r.codigo_cliente = c.codigo_cliente "
      + "INNER JOIN habitaciones h ON r.codigo_habitacion = h.codigo_habitacion ";

    public ReservaDAO() {
        this.db = ConexionMySQL.getInstance();
    }

    public boolean insertar(Reserva reserva) {
        if (buscarPorCodigo(reserva.getCodigoReserva()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una reserva con el código: " + reserva.getCodigoReserva());
        }
        String sql = "INSERT INTO reservas (codigo_reserva, codigo_cliente, codigo_habitacion, fecha_entrada, fecha_salida, "
                   + "cantidad_huespedes, temporada_alta, noches, tarifa_base_noche, subtotal_base, cargos_adicionales, costo_total, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reserva.getCodigoReserva());
            ps.setString(2, reserva.getCliente().getCodigoCliente());
            ps.setString(3, reserva.getHabitacion().getCodigoHabitacion());
            ps.setDate(4, Date.valueOf(reserva.getFechaEntrada()));
            ps.setDate(5, Date.valueOf(reserva.getFechaSalida()));
            ps.setInt(6, reserva.getCantidadHuespedes());
            ps.setBoolean(7, reserva.isTemporadaAlta());
            ps.setInt(8, reserva.getNoches());
            ps.setDouble(9, reserva.getTarifaBaseNoche());
            ps.setDouble(10, reserva.getSubtotalBase());
            ps.setDouble(11, reserva.getCargosAdicionales());
            ps.setDouble(12, reserva.getCostoTotal());
            ps.setString(13, reserva.getEstado().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al registrar reserva: " + e.getMessage(), e);
        }
    }

    public boolean actualizar(Reserva reserva) {
        String sql = "UPDATE reservas SET codigo_habitacion = ?, fecha_entrada = ?, fecha_salida = ?, "
                   + "cantidad_huespedes = ?, temporada_alta = ?, noches = ?, tarifa_base_noche = ?, "
                   + "subtotal_base = ?, cargos_adicionales = ?, costo_total = ?, estado = ? "
                   + "WHERE codigo_reserva = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reserva.getHabitacion().getCodigoHabitacion());
            ps.setDate(2, Date.valueOf(reserva.getFechaEntrada()));
            ps.setDate(3, Date.valueOf(reserva.getFechaSalida()));
            ps.setInt(4, reserva.getCantidadHuespedes());
            ps.setBoolean(5, reserva.isTemporadaAlta());
            ps.setInt(6, reserva.getNoches());
            ps.setDouble(7, reserva.getTarifaBaseNoche());
            ps.setDouble(8, reserva.getSubtotalBase());
            ps.setDouble(9, reserva.getCargosAdicionales());
            ps.setDouble(10, reserva.getCostoTotal());
            ps.setString(11, reserva.getEstado().name());
            ps.setString(12, reserva.getCodigoReserva());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al actualizar reserva: " + e.getMessage(), e);
        }
    }

    public Optional<Reserva> buscarPorCodigo(String codigoReserva) {
        if (codigoReserva == null || codigoReserva.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = SELECT_JOIN_BASE + "WHERE r.codigo_reserva = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoReserva.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearReserva(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al buscar reserva: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Reserva> listarTodas() {
        List<Reserva> lista = new ArrayList<>();
        String sql = SELECT_JOIN_BASE + "ORDER BY r.fecha_entrada DESC, r.codigo_reserva DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al listar reservas: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Proceso 4.4: Consultar reservas por código, cliente, habitación, fechas o estado.
     */
    public List<Reserva> buscarPorCriterio(String textoFiltro, String estadoFiltro) {
        List<Reserva> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_JOIN_BASE).append("WHERE 1=1 ");
        boolean tieneTexto = textoFiltro != null && !textoFiltro.trim().isEmpty();
        boolean tieneEstado = estadoFiltro != null && !estadoFiltro.trim().isEmpty() && !"TODOS".equalsIgnoreCase(estadoFiltro.trim());

        if (tieneTexto) {
            sql.append("AND (UPPER(r.codigo_reserva) LIKE UPPER(?) "
                     + "OR UPPER(c.codigo_cliente) LIKE UPPER(?) "
                     + "OR UPPER(c.nombre_completo) LIKE UPPER(?) "
                     + "OR UPPER(h.codigo_habitacion) LIKE UPPER(?) "
                     + "OR CAST(h.numero AS CHAR) LIKE ?) ");
        }
        if (tieneEstado) {
            sql.append("AND UPPER(r.estado) = UPPER(?) ");
        }
        sql.append("ORDER BY r.fecha_entrada DESC, r.codigo_reserva DESC");

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (tieneTexto) {
                String p = "%" + textoFiltro.trim() + "%";
                ps.setString(idx++, p);
                ps.setString(idx++, p);
                ps.setString(idx++, p);
                ps.setString(idx++, p);
                ps.setString(idx++, p);
            }
            if (tieneEstado) {
                ps.setString(idx, estadoFiltro.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error en MySQL al filtrar reservas: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Proceso 3.3: Obtiene las reservas CONFIRMADAS que se superponen con el período [entrada, salida)
     * para una habitación determinada (excluyendo opcionalmente el código de una reserva en edición).
     */
    public List<Reserva> buscarConflictosHabitacion(String codigoHabitacion, LocalDate entrada,
                                                    LocalDate salida, String excluirCodigoReserva) {
        Reserva.validarPeriodo(entrada, salida);
        List<Reserva> conflictos = new ArrayList<>();
        String sql = SELECT_JOIN_BASE
                + "WHERE r.codigo_habitacion = ? "
                + "AND r.estado = 'CONFIRMADA' "
                + "AND r.fecha_entrada < ? AND r.fecha_salida > ? "
                + (excluirCodigoReserva != null ? "AND r.codigo_reserva <> ? " : "");
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoHabitacion.trim().toUpperCase());
            ps.setDate(2, Date.valueOf(salida));
            ps.setDate(3, Date.valueOf(entrada));
            if (excluirCodigoReserva != null) {
                ps.setString(4, excluirCodigoReserva.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conflictos.add(mapearReserva(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al verificar disponibilidad de fechas en MySQL: " + e.getMessage(), e);
        }
        return conflictos;
    }

    public int contarReservasConfirmadasParaHabitacion(String codigoHabitacion) {
        String sql = "SELECT COUNT(*) FROM reservas WHERE codigo_habitacion = ? AND estado = 'CONFIRMADA'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoHabitacion.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al contar reservas activas: " + e.getMessage(), e);
        }
        return 0;
    }

    public String generarSiguienteCodigoReserva() {
        String sql = "SELECT codigo_reserva FROM reservas ORDER BY codigo_reserva DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String ultimo = rs.getString(1);
                if (ultimo != null && ultimo.startsWith("RES-")) {
                    try {
                        int num = Integer.parseInt(ultimo.substring(4));
                        return String.format("RES-%04d", num + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return "RES-" + (1000 + listarTodas().size() + 1);
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(
            rs.getString("codigo_cliente"),
            rs.getString("nombre_completo"),
            rs.getString("documento"),
            rs.getString("telefono"),
            rs.getString("email"),
            rs.getBoolean("cliente_activo")
        );
        Habitacion habitacion = HabitacionFactory.crearHabitacion(
            rs.getString("tipo"),
            rs.getString("codigo_habitacion"),
            rs.getInt("numero"),
            rs.getInt("capacidad"),
            rs.getDouble("precio_noche"),
            EstadoHabitacion.valueOf(rs.getString("estado_habitacion")),
            rs.getString("comodidades")
        );
        return new Reserva(
            rs.getString("codigo_reserva"),
            cliente,
            habitacion,
            rs.getDate("fecha_entrada").toLocalDate(),
            rs.getDate("fecha_salida").toLocalDate(),
            rs.getInt("cantidad_huespedes"),
            rs.getBoolean("temporada_alta"),
            rs.getInt("noches"),
            rs.getDouble("tarifa_base_noche"),
            rs.getDouble("subtotal_base"),
            rs.getDouble("cargos_adicionales"),
            rs.getDouble("costo_total"),
            EstadoReserva.valueOf(rs.getString("estado_reserva"))
        );
    }
}
