package com.hotel.mvc.view;

import com.hotel.mvc.controller.HotelController;
import com.hotel.mvc.model.Cliente;
import com.hotel.mvc.model.EstadoHabitacion;
import com.hotel.mvc.model.Habitacion;
import com.hotel.mvc.model.Reserva;
import com.hotel.mvc.model.Usuario;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Vista Gráfica Java Swing (RF-05) integrada al mismo HotelController MVC.
 * Incluye JFrame, JPanel, JLabel, JTextField, JPasswordField, JButton, JComboBox, JTable y JOptionPane.
 */
public class SwingHotelView extends JFrame {

    private final HotelController controller;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(cardLayout);

    // Componentes de Login (RF-01)
    private final JTextField txtLoginUser = new JTextField(20);
    private final JPasswordField txtLoginPass = new JPasswordField(20);
    private final JLabel lblUsuarioActivo = new JLabel("Sin sesión activa");

    // Modelos de tablas (RF-05)
    private final DefaultTableModel modeloClientes = new DefaultTableModel(
            new Object[]{"Código", "Nombre Completo", "Documento", "Teléfono", "Correo", "Activo"}, 0);
    private final DefaultTableModel modeloHabitaciones = new DefaultTableModel(
            new Object[]{"Código", "Número", "Tipo", "Capacidad", "Precio/Noche", "Cargo Extra", "Estado", "Comodidades"}, 0);
    private final DefaultTableModel modeloDisponibles = new DefaultTableModel(
            new Object[]{"Código", "Número", "Tipo", "Capacidad", "Precio/Noche", "Comodidades"}, 0);
    private final DefaultTableModel modeloReservas = new DefaultTableModel(
            new Object[]{"Código", "Cliente", "Habitación", "Entrada", "Salida", "Noches", "Huésp.", "Temp.Alta", "Subtotal", "Extra", "Total", "Estado"}, 0);

    // Combos de Reserva
    private final JComboBox<String> cmbResCliente = new JComboBox<>();
    private final JComboBox<String> cmbResHabitacion = new JComboBox<>();

    public SwingHotelView(HotelController controller) {
        super("Hotel Gran Sol & Montaña - Sistema MVC (Java Swing + Web + MySQL 8.0)");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);

        rootPanel.add(construirPanelLogin(), "LOGIN");
        rootPanel.add(construirPanelPrincipal(), "APP");
        setContentPane(rootPanel);
        cardLayout.show(rootPanel, "LOGIN");
    }

    private JPanel construirPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel titulo = new JLabel("Hotel Gran Sol & Montaña - Inicio de Sesión", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        card.add(titulo, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("UES · FIA · EISI · POO115 (Autenticación SHA-256 + MySQL)", SwingConstants.CENTER);
        sub.setForeground(new Color(100, 116, 139));
        card.add(sub, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(new JLabel("Usuario o Correo *:"), gbc);
        gbc.gridx = 1;
        card.add(txtLoginUser, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(new JLabel("Contraseña *:"), gbc);
        gbc.gridx = 1;
        card.add(txtLoginPass, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBackground(new Color(30, 64, 175));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(e -> ejecutarLogin());
        card.add(btnLogin, gbc);

        panel.add(card);
        return panel;
    }

    private JPanel construirPanelPrincipal() {
        JPanel main = new JPanel(new BorderLayout());

        // Barra superior con Cierre de Sesión (RF-06)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(15, 23, 42));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        lblUsuarioActivo.setForeground(Color.WHITE);
        lblUsuarioActivo.setFont(new Font("SansSerif", Font.BOLD, 13));
        topBar.add(lblUsuarioActivo, BorderLayout.WEST);

        JButton btnLogout = new JButton("Cerrar Sesión (RF-06)");
        btnLogout.setBackground(new Color(220, 38, 38));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            controller.cerrarSesionLocal();
            txtLoginPass.setText("");
            cardLayout.show(rootPanel, "LOGIN");
            JOptionPane.showMessageDialog(this, "Sesión cerrada correctamente.", "Cierre de Sesión", JOptionPane.INFORMATION_MESSAGE);
        });
        topBar.add(btnLogout, BorderLayout.EAST);
        main.add(topBar, BorderLayout.NORTH);

        // Pestañas para los 5 procesos
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Gestión de Clientes", construirTabClientes());
        tabs.addTab("2. Gestión de Habitaciones", construirTabHabitaciones());
        tabs.addTab("3 y 5. Disponibilidad y Costos", construirTabDisponibilidad());
        tabs.addTab("4. Gestión de Reservas", construirTabReservas());
        main.add(tabs, BorderLayout.CENTER);

        return main;
    }

    private JPanel construirTabClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Registrar / Modificar / Eliminar Cliente"));
        JTextField txtCod = new JTextField("CLI-009");
        JTextField txtNom = new JTextField();
        JTextField txtDoc = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtEmail = new JTextField();

        form.add(new JLabel("Código *:"));
        form.add(new JLabel("Nombre Completo *:"));
        form.add(new JLabel("Documento *:"));
        form.add(new JLabel("Teléfono *:"));
        form.add(new JLabel("Correo *:"));
        form.add(new JLabel("Acciones:"));

        form.add(txtCod);
        form.add(txtNom);
        form.add(txtDoc);
        form.add(txtTel);
        form.add(txtEmail);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnGuardar = new JButton("Registrar");
        JButton btnMod = new JButton("Modificar");
        JButton btnElim = new JButton("Eliminar");
        acciones.add(btnGuardar);
        acciones.add(btnMod);
        acciones.add(btnElim);
        form.add(acciones);

        JTable tabla = new JTable(modeloClientes);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r >= 0) {
                txtCod.setText(String.valueOf(modeloClientes.getValueAt(r, 0)));
                txtNom.setText(String.valueOf(modeloClientes.getValueAt(r, 1)));
                txtDoc.setText(String.valueOf(modeloClientes.getValueAt(r, 2)));
                txtTel.setText(String.valueOf(modeloClientes.getValueAt(r, 3)));
                txtEmail.setText(String.valueOf(modeloClientes.getValueAt(r, 4)));
            }
        });

        btnGuardar.addActionListener(e -> {
            try {
                controller.registrarCliente(txtCod.getText(), txtNom.getText(), txtDoc.getText(), txtTel.getText(), txtEmail.getText());
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Cliente registrado en MySQL.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnMod.addActionListener(e -> {
            try {
                controller.modificarCliente(txtCod.getText(), txtNom.getText(), txtDoc.getText(), txtTel.getText(), txtEmail.getText(), true);
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Cliente actualizado en MySQL.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnElim.addActionListener(e -> {
            try {
                controller.eliminarCliente(txtCod.getText());
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Cliente eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Restricción de Integridad", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTabHabitaciones() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 7, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Registrar / Modificar / Eliminar Habitación"));
        JTextField txtCod = new JTextField("HAB-104");
        JTextField txtNum = new JTextField("104");
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"SIMPLE", "DOBLE", "SUITE"});
        JTextField txtCap = new JTextField("2");
        JTextField txtPrecio = new JTextField("65.00");
        JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"DISPONIBLE", "OCUPADA", "FUERA_DE_SERVICIO"});

        form.add(new JLabel("Código *:"));
        form.add(new JLabel("Número *:"));
        form.add(new JLabel("Categoría *:"));
        form.add(new JLabel("Capacidad *:"));
        form.add(new JLabel("Precio/Noche *:"));
        form.add(new JLabel("Estado *:"));
        form.add(new JLabel("Acciones:"));

        form.add(txtCod);
        form.add(txtNum);
        form.add(cmbTipo);
        form.add(txtCap);
        form.add(txtPrecio);
        form.add(cmbEstado);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnCrear = new JButton("Registrar");
        JButton btnMod = new JButton("Modificar");
        JButton btnElim = new JButton("Eliminar");
        acciones.add(btnCrear);
        acciones.add(btnMod);
        acciones.add(btnElim);
        form.add(acciones);

        JTable tabla = new JTable(modeloHabitaciones);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r >= 0) {
                txtCod.setText(String.valueOf(modeloHabitaciones.getValueAt(r, 0)));
                txtNum.setText(String.valueOf(modeloHabitaciones.getValueAt(r, 1)));
                cmbTipo.setSelectedItem(String.valueOf(modeloHabitaciones.getValueAt(r, 2)));
                txtCap.setText(String.valueOf(modeloHabitaciones.getValueAt(r, 3)));
                txtPrecio.setText(String.valueOf(modeloHabitaciones.getValueAt(r, 4)));
                cmbEstado.setSelectedItem(String.valueOf(modeloHabitaciones.getValueAt(r, 6)));
            }
        });

        btnCrear.addActionListener(e -> {
            try {
                controller.registrarHabitacion(
                    txtCod.getText(), Integer.parseInt(txtNum.getText().trim()),
                    String.valueOf(cmbTipo.getSelectedItem()), Integer.parseInt(txtCap.getText().trim()),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    EstadoHabitacion.valueOf(String.valueOf(cmbEstado.getSelectedItem())), null
                );
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Habitación registrada en MySQL.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnMod.addActionListener(e -> {
            try {
                controller.modificarHabitacion(
                    txtCod.getText(), Integer.parseInt(txtNum.getText().trim()),
                    String.valueOf(cmbTipo.getSelectedItem()), Integer.parseInt(txtCap.getText().trim()),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    EstadoHabitacion.valueOf(String.valueOf(cmbEstado.getSelectedItem())), null
                );
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Habitación modificada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnElim.addActionListener(e -> {
            try {
                controller.eliminarHabitacion(txtCod.getText());
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Habitación eliminada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Restricción", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTabDisponibilidad() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filtro.setBorder(BorderFactory.createTitledBorder("Proceso 3: Consulta de Disponibilidad y Proceso 5: Cálculo Polimórfico"));
        JTextField txtEntrada = new JTextField(LocalDate.now().toString(), 10);
        JTextField txtSalida = new JTextField(LocalDate.now().plusDays(2).toString(), 10);
        JTextField txtCap = new JTextField("1", 4);
        JCheckBox chkTempAlta = new JCheckBox("Temporada Alta");
        JButton btnBuscar = new JButton("Consultar Disponibles");
        JButton btnCotizar = new JButton("Calcular Costo Habitación Seleccionada");

        filtro.add(new JLabel("Entrada (YYYY-MM-DD):"));
        filtro.add(txtEntrada);
        filtro.add(new JLabel("Salida (YYYY-MM-DD):"));
        filtro.add(txtSalida);
        filtro.add(new JLabel("Capacidad Mín.:"));
        filtro.add(txtCap);
        filtro.add(chkTempAlta);
        filtro.add(btnBuscar);
        filtro.add(btnCotizar);

        JTable tabla = new JTable(modeloDisponibles);

        btnBuscar.addActionListener(e -> {
            try {
                LocalDate ent = LocalDate.parse(txtEntrada.getText().trim());
                LocalDate sal = LocalDate.parse(txtSalida.getText().trim());
                int cap = Integer.parseInt(txtCap.getText().trim());
                List<Habitacion> disp = controller.consultarDisponibilidadPorPeriodo(ent, sal, cap, "TODOS");
                modeloDisponibles.setRowCount(0);
                for (Habitacion h : disp) {
                    modeloDisponibles.addRow(new Object[]{
                        h.getCodigoHabitacion(), h.getNumero(), h.getTipo(), h.getCapacidad(), h.getPrecioNoche(), h.getComodidades()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación de Período", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCotizar.addActionListener(e -> {
            int r = tabla.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una habitación de la tabla para calcular su costo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String codHab = String.valueOf(modeloDisponibles.getValueAt(r, 0));
                Habitacion h = controller.buscarHabitacionPorCodigo(codHab).orElseThrow();
                LocalDate ent = LocalDate.parse(txtEntrada.getText().trim());
                LocalDate sal = LocalDate.parse(txtSalida.getText().trim());
                Reserva.validarPeriodo(ent, sal);
                int noches = (int) ChronoUnit.DAYS.between(ent, sal);
                boolean ta = chkTempAlta.isSelected();
                String msg = String.format(
                    "Habitación #%d (%s)\nNoches: %d | Tarifa Base: $%.2f\nSubtotal Base: $%.2f\nCargos Adicionales: $%.2f\nCOSTO TOTAL: $%.2f",
                    h.getNumero(), h.getTipo(), noches, h.getPrecioNoche(),
                    h.calcularSubtotalBase(noches), h.calcularCargosAdicionales(noches, ta), h.calcularCosto(noches, ta)
                );
                JOptionPane.showMessageDialog(this, msg, "Proceso 5 - Cálculo Polimórfico de Costo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(filtro, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirTabReservas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(2, 7, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Proceso 4: Registrar / Modificar / Cancelar Reservas"));
        JTextField txtCod = new JTextField();
        JTextField txtEnt = new JTextField(LocalDate.now().plusDays(5).toString());
        JTextField txtSal = new JTextField(LocalDate.now().plusDays(7).toString());
        JTextField txtHuesp = new JTextField("1");
        JCheckBox chkTA = new JCheckBox("Temporada Alta");

        form.add(new JLabel("Código (Opcional):"));
        form.add(new JLabel("Cliente *:"));
        form.add(new JLabel("Habitación *:"));
        form.add(new JLabel("Entrada (YYYY-MM-DD):"));
        form.add(new JLabel("Salida (YYYY-MM-DD):"));
        form.add(new JLabel("Huéspedes *:"));
        form.add(new JLabel("Temporada / Acción:"));

        form.add(txtCod);
        form.add(cmbResCliente);
        form.add(cmbResHabitacion);
        form.add(txtEnt);
        form.add(txtSal);
        form.add(txtHuesp);

        JPanel acc = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar Sel.");
        acc.add(chkTA);
        acc.add(btnConfirmar);
        acc.add(btnCancelar);
        form.add(acc);

        JTable tabla = new JTable(modeloReservas);

        btnConfirmar.addActionListener(e -> {
            try {
                String codCli = String.valueOf(cmbResCliente.getSelectedItem()).split(" - ")[0];
                String codHab = String.valueOf(cmbResHabitacion.getSelectedItem()).split(" - ")[0];
                Reserva r = controller.registrarReserva(
                    txtCod.getText(), codCli, codHab,
                    LocalDate.parse(txtEnt.getText().trim()),
                    LocalDate.parse(txtSal.getText().trim()),
                    Integer.parseInt(txtHuesp.getText().trim()),
                    chkTA.isSelected()
                );
                recargarTodo();
                JOptionPane.showMessageDialog(this, "Reserva confirmada: " + r.getCodigoReserva() + " | Total: $" + r.getCostoTotal(),
                        "Reserva Confirmada", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación de Reserva", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> {
            int row = tabla.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String codRes = String.valueOf(modeloReservas.getValueAt(row, 0));
            if (JOptionPane.showConfirmDialog(this, "¿Confirma cancelar la reserva " + codRes + "?", "Confirmación",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    controller.cancelarReserva(codRes);
                    recargarTodo();
                    JOptionPane.showMessageDialog(this, "Reserva " + codRes + " cancelada.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private void ejecutarLogin() {
        try {
            Usuario u = controller.iniciarSesion(txtLoginUser.getText(), new String(txtLoginPass.getPassword()));
            lblUsuarioActivo.setText("Sesión activa: " + u.getNombreCompleto() + " [" + u.getRol() + "]");
            recargarTodo();
            cardLayout.show(rootPanel, "APP");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Autenticación (RF-01)", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recargarTodo() {
        modeloClientes.setRowCount(0);
        cmbResCliente.removeAllItems();
        for (Cliente c : controller.listarClientes()) {
            modeloClientes.addRow(new Object[]{
                c.getCodigoCliente(), c.getNombreCompleto(), c.getDocumento(), c.getTelefono(), c.getEmail(), c.isActivo()
            });
            if (c.isActivo()) {
                cmbResCliente.addItem(c.getCodigoCliente() + " - " + c.getNombreCompleto());
            }
        }

        modeloHabitaciones.setRowCount(0);
        cmbResHabitacion.removeAllItems();
        for (Habitacion h : controller.listarHabitaciones()) {
            modeloHabitaciones.addRow(new Object[]{
                h.getCodigoHabitacion(), h.getNumero(), h.getTipo(), h.getCapacidad(),
                h.getPrecioNoche(), h.getCargoExtraNoche(), h.getEstado().name(), h.getComodidades()
            });
            cmbResHabitacion.addItem(h.getCodigoHabitacion() + " - Hab.#" + h.getNumero() + " (" + h.getTipo() + ")");
        }

        modeloReservas.setRowCount(0);
        for (Reserva r : controller.listarReservas()) {
            modeloReservas.addRow(new Object[]{
                r.getCodigoReserva(), r.getCliente().getNombreCompleto(),
                "#" + r.getHabitacion().getNumero() + " (" + r.getHabitacion().getTipo() + ")",
                r.getFechaEntrada(), r.getFechaSalida(), r.getNoches(), r.getCantidadHuespedes(),
                r.isTemporadaAlta() ? "SÍ" : "NO",
                r.getSubtotalBase(), r.getCargosAdicionales(), r.getCostoTotal(), r.getEstado().name()
            });
        }
    }
}
