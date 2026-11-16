package com.hotel.mvc.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generador CLI de Diagramas Técnicos en alta resolución (PNG) para la carpeta 03_UML.
 * Genera:
 *  1. 01_Diagrama_Casos_De_Uso.png
 *  2. 02_Diagrama_Jerarquico_Sistema.png
 *  3. 03_Flujograma_Procesos_MVC.png
 *  4. 04_Diagrama_Clases_UML_MVC.png
 *  5. 05_Diagrama_Secuencia_Reserva_MVC.png
 *  6. 06_Diagrama_Estados_Reserva_Y_Habitacion.png
 *  7. 07_Diagrama_Entidad_Relacion_Conceptual.png
 *  8. 08_Diagrama_Logico_Relacional_MySQL.png
 *  9. 09_Diagrama_Fisico_MySQL80.png
 */
public class GeneradorDiagramasUML {

    private static final Color BG_COLOR = new Color(248, 250, 252);
    private static final Color HEADER_BG = new Color(15, 23, 42);
    private static final Color PRIMARY = new Color(30, 64, 175);
    private static final Color ACCENT = new Color(2, 132, 199);
    private static final Color SUCCESS = new Color(22, 101, 52);
    private static final Color WARNING = new Color(180, 83, 9);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(148, 163, 184);

    public static void main(String[] args) throws Exception {
        File outDir = args.length > 0 ? new File(args[0]) : new File("../03_UML");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        generarCasosDeUso(new File(outDir, "01_Diagrama_Casos_De_Uso.png"));
        generarDiagramaJerarquico(new File(outDir, "02_Diagrama_Jerarquico_Sistema.png"));
        generarFlujograma(new File(outDir, "03_Flujograma_Procesos_MVC.png"));
        generarDiagramaClasesMVC(new File(outDir, "04_Diagrama_Clases_UML_MVC.png"));
        generarDiagramaSecuencia(new File(outDir, "05_Diagrama_Secuencia_Reserva_MVC.png"));
        generarDiagramaEstados(new File(outDir, "06_Diagrama_Estados_Reserva_Y_Habitacion.png"));
        generarDiagramaERConceptual(new File(outDir, "07_Diagrama_Entidad_Relacion_Conceptual.png"));
        generarDiagramaLogicoMySQL(new File(outDir, "08_Diagrama_Logico_Relacional_MySQL.png"));
        generarDiagramaFisicoMySQL(new File(outDir, "09_Diagrama_Fisico_MySQL80.png"));
        System.out.println("[OK] 9 diagramas técnicos generados exitosamente en: " + outDir.getAbsolutePath());
    }

    private static Graphics2D crearLienzo(BufferedImage img, String titulo, String subtitulo) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setColor(HEADER_BG);
        g.fillRect(0, 0, img.getWidth(), 72);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString(titulo, 28, 34);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(186, 230, 253));
        g.drawString(subtitulo, 28, 56);
        return g;
    }

    private static void dibujarCajaClase(Graphics2D g, int x, int y, int w, int h,
                                         String estereotipo, String nombre, Color headerColor,
                                         String[] atributos, String[] metodos) {
        g.setColor(new Color(15, 23, 42, 22));
        g.fillRoundRect(x + 4, y + 4, w, h, 10, 10);

        g.setColor(CARD_BG);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(headerColor);
        g.fillRoundRect(x, y, w, 44, 10, 10);
        g.fillRect(x, y + 34, w, 10);

        g.setColor(Color.WHITE);
        int textY = y + 18;
        if (estereotipo != null && !estereotipo.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.ITALIC, 11));
            centrarTexto(g, estereotipo, x, w, textY);
            textY += 16;
        } else {
            textY += 8;
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        centrarTexto(g, nombre, x, w, textY);

        g.setColor(headerColor);
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        int curY = y + 60;
        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        for (String attr : atributos) {
            g.drawString(attr, x + 10, curY);
            curY += 15;
        }
        g.setColor(BORDER);
        g.drawLine(x, curY - 8, x + w, curY - 8);
        curY += 8;
        g.setColor(new Color(15, 23, 42));
        for (String m : metodos) {
            g.drawString(m, x + 10, curY);
            curY += 15;
        }
    }

    private static void dibujarCajaNodo(Graphics2D g, int x, int y, int w, int h,
                                        String titulo, String[] lineas, Color colorBorde, Color colorHeader) {
        g.setColor(CARD_BG);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(colorHeader);
        g.fillRoundRect(x, y, w, 30, 10, 10);
        g.fillRect(x, y + 20, w, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        centrarTexto(g, titulo, x, w, y + 20);

        g.setColor(colorBorde);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int cy = y + 48;
        for (String l : lineas) {
            g.drawString(l, x + 10, cy);
            cy += 16;
        }
    }

    private static void centrarTexto(Graphics2D g, String txt, int x, int w, int y) {
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(txt)) / 2;
        g.drawString(txt, tx, y);
    }

    private static void dibujarFlecha(Graphics2D g, int x1, int y1, int x2, int y2, String etiqueta, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int len = 10;
        int ax1 = (int) (x2 - len * Math.cos(angle - Math.PI / 6));
        int ay1 = (int) (y2 - len * Math.sin(angle - Math.PI / 6));
        int ax2 = (int) (x2 - len * Math.cos(angle + Math.PI / 6));
        int ay2 = (int) (y2 - len * Math.sin(angle + Math.PI / 6));
        g.fillPolygon(new int[]{x2, ax1, ax2}, new int[]{y2, ay1, ay2}, 3);
        if (etiqueta != null && !etiqueta.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            int mx = (x1 + x2) / 2;
            int my = (y1 + y2) / 2 - 6;
            g.drawString(etiqueta, mx - 20, my);
        }
    }

    // 1. DIAGRAMA DE CASOS DE USO
    private static void generarCasosDeUso(File out) throws Exception {
        BufferedImage img = new BufferedImage(1350, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 1. DIAGRAMA DE CASOS DE USO UML - SISTEMA DE RESERVAS DE UN HOTEL (CLAVE 1)",
            "Universidad de El Salvador · POO115 · Actores: Recepcionista (Principal) y Huésped (Secundario)");

        // Límite del sistema
        g.setColor(Color.WHITE);
        g.fillRoundRect(300, 100, 740, 680, 16, 16);
        g.setColor(PRIMARY);
        g.setStroke(new BasicStroke(2.2f));
        g.drawRoundRect(300, 100, 740, 680, 16, 16);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("<<Sistema>> Sistema de Reservas Hotel Gran Sol & Montaña (MVC + MySQL 8.0)", 360, 130);

        // Actores
        dibujarActor(g, 130, 340, "Recepcionista (Principal)");
        dibujarActor(g, 1190, 480, "Huésped (Secundario)");

        String[] casos = {
            "CU-01 Iniciar y Cerrar Sesión (Login SHA-256 / Logout)",
            "CU-02 Gestionar Clientes (Registrar, Buscar, Modificar, Eliminar)",
            "CU-03 Gestionar Habitaciones (Registrar, Estado, Modificar, Eliminar)",
            "CU-04 Consultar Disponibilidad por Período de Fechas",
            "CU-05 Realizar y Modificar Reserva de Alojamiento",
            "CU-06 Cancelar Reserva Confirmada y Liberar Habitación",
            "CU-07Consultar Historial y Filtros de Reservas",
            "CU-08 Calcular Costo Polimórfico de Reserva (Simple/Doble/Suite)"
        };

        int cy = 165;
        for (int i = 0; i < casos.length; i++) {
            int ey = cy + i * 72;
            g.setColor(new Color(219, 234, 254));
            g.fillOval(410, ey, 500, 52);
            g.setColor(PRIMARY);
            g.setStroke(new BasicStroke(1.8f));
            g.drawOval(410, ey, 500, 52);
            g.setColor(new Color(15, 23, 42));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            centrarTexto(g, casos[i], 410, 500, ey + 31);

            // Línea desde Recepcionista
            g.setColor(new Color(71, 85, 105));
            g.drawLine(175, 370, 410, ey + 26);
        }

        // Huésped participa en CU-04, CU-05 y CU-06
        g.setColor(new Color(71, 85, 105));
        g.drawLine(1145, 500, 910, 165 + 3 * 72 + 26);
        g.drawLine(1145, 500, 910, 165 + 4 * 72 + 26);
        g.drawLine(1145, 500, 910, 165 + 5 * 72 + 26);

        // Relación <<include>> entre CU-05 y CU-08
        dibujarFlecha(g, 660, 165 + 4 * 72 + 52, 660, 165 + 7 * 72, "<<include>>", SUCCESS);

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    private static void dibujarActor(Graphics2D g, int x, int y, String nombre) {
        g.setColor(PRIMARY);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(x - 20, y - 50, 40, 40);
        g.drawLine(x, y - 10, x, y + 55);
        g.drawLine(x - 35, y + 15, x + 35, y + 15);
        g.drawLine(x, y + 55, x - 28, y + 100);
        g.drawLine(x, y + 55, x + 28, y + 100);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        centrarTexto(g, nombre, x - 90, 180, y + 125);
    }

    // 2. DIAGRAMA JERÁRQUICO DEL SISTEMA (SECCIÓN E.7)
    private static void generarDiagramaJerarquico(File out) throws Exception {
        BufferedImage img = new BufferedImage(1480, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 2. DIAGRAMA JERÁRQUICO DE MÓDULOS Y PROCESOS DEL SISTEMA (SECCIÓN 7)",
            "Organización Funcional del Sistema de Reservas del Hotel Gran Sol & Montaña (Clave 1)");

        dibujarCajaNodo(g, 520, 100, 440, 70,
            "SISTEMA DE RESERVAS HOTEL GRAN SOL & MONTAÑA (MVC)",
            new String[]{"Controlador Central: HotelController + MySQL 8.0 (JDBC)"}, PRIMARY, PRIMARY);

        String[] titulos = {
            "MÓDULO 0: SEGURIDAD (RF-01/06)",
            "PROCESO 1: GESTIÓN CLIENTES",
            "PROCESO 2: HABITACIONES",
            "PROCESO 3: DISPONIBILIDAD",
            "PROCESO 4: GESTIÓN RESERVAS",
            "PROCESO 5: CÁLCULO DE COSTOS"
        };
        String[][] subprocesos = {
            {"0.1 Pantalla de Login", "0.2 Hash SHA-256 + Salt", "0.3 Validación en MySQL", "0.4 Token Web / Sesión", "0.5 Cierre de Sesión (Logout)"},
            {"1.1 Registrar Cliente", "1.2 Buscar (Código/Doc/Nombre)", "1.3 Consultar Listado", "1.4 Modificar Contacto", "1.5 Eliminar (Sin Reservas)"},
            {"2.1 Registrar Habitación", "2.2 Consultar Catálogo", "2.3 Buscar Habitación", "2.4 Modificar Precio/Cap.", "2.5 Cambiar Estado / 2.6 Eliminar"},
            {"3.1 Ingresar Entrada/Salida", "3.2 Validar Salida > Entrada", "3.3 Consultar Confirmadas", "3.4 Filtrar Superposición", "3.5 Mostrar Disponibles"},
            {"4.1 Registrar Reserva", "4.2 Validar Capacidad/Fechas", "4.3 Confirmar Reserva", "4.4 Consultar / Filtrar", "4.5 Cancelar / 4.6 Modificar"},
            {"5.1 Calcular Noches", "5.2 Obtener Tarifa Base", "5.3 Calcular Subtotal Base", "5.4 Polimorfismo Categoría", "5.5 Presentar Costo Total"}
        };

        int startX = 30;
        int w = 225;
        int gap = 14;
        for (int i = 0; i < 6; i++) {
            int bx = startX + i * (w + gap);
            int by = 260;
            g.setColor(PRIMARY);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(740, 170, bx + w / 2, by);
            dibujarCajaNodo(g, bx, by, w, 65, titulos[i],
                new String[]{"Módulo Funcional MVC"}, ACCENT, ACCENT);

            for (int j = 0; j < subprocesos[i].length; j++) {
                int sy = 365 + j * 78;
                g.setColor(BORDER);
                g.drawLine(bx + w / 2, by + 65, bx + w / 2, sy);
                dibujarCajaNodo(g, bx + 8, sy, w - 16, 62, subprocesos[i][j],
                    new String[]{"Validado + Persistido en MySQL"}, SUCCESS, new Color(30, 64, 175));
            }
        }

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 3. FLUJOGRAMA DE PROCESOS (SECCIÓN E.8)
    private static void generarFlujograma(File out) throws Exception {
        BufferedImage img = new BufferedImage(1380, 860, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 3. FLUJOGRAMA DE EJECUCIÓN: LOGIN, VALIDACIÓN, DISPONIBILIDAD Y RESERVA (SECCIÓN 8)",
            "Flujo Algorítmico Principal del Sistema de Información Hotelero (MVC + MySQL 8.0)");

        dibujarCajaNodo(g, 520, 95, 340, 55, "INICIO DEL SISTEMA (CLI / Web / Swing)",
            new String[]{"Inicializa ConexionMySQL Singleton y Servidor HTTP"}, SUCCESS, SUCCESS);
        dibujarFlecha(g, 690, 150, 690, 185, null, PRIMARY);

        dibujarCajaNodo(g, 490, 185, 400, 65, "1. Capturar Credenciales en Login (RF-01)",
            new String[]{"Entrada: username/email y password"}, PRIMARY, PRIMARY);
        dibujarFlecha(g, 690, 250, 690, 285, null, PRIMARY);

        dibujarCajaNodo(g, 460, 285, 460, 70, "2. ¿Campos vacíos o Hash SHA-256(salt+pass) inválido?",
            new String[]{"Consulta UsuarioDAO.buscarPorCredencial() en MySQL"}, WARNING, WARNING);

        // Rama de rechazo Login
        dibujarCajaNodo(g, 60, 285, 310, 70, "Mostrar Mensaje de Error Seguro",
            new String[]{"Bloquea acceso y retorna al formulario Login"}, new Color(220, 38, 38), new Color(220, 38, 38));
        dibujarFlecha(g, 460, 320, 370, 320, "SÍ (Inválido)", new Color(220, 38, 38));
        dibujarFlecha(g, 215, 285, 490, 215, "Reintentar", new Color(220, 38, 38));

        // Rama éxito Login
        dibujarFlecha(g, 690, 355, 690, 395, "NO (Válido)", SUCCESS);
        dibujarCajaNodo(g, 460, 395, 460, 68, "3. Acceso a Módulos Protegidos MVC (Procesos 1 a 5)",
            new String[]{"Seleccionar Cliente, Habitación, Fecha Entrada, Salida y Huéspedes"}, PRIMARY, PRIMARY);
        dibujarFlecha(g, 690, 463, 690, 500, null, PRIMARY);

        dibujarCajaNodo(g, 440, 500, 500, 72, "4. ¿Fechas válidas (Salida > Entrada), Capacidad OK y Sin Superposición?",
            new String[]{"Verifica: entradaExistente < salidaSolicitada AND salidaExistente > entradaSolicitada"}, WARNING, WARNING);

        dibujarCajaNodo(g, 990, 500, 340, 72, "Rechazar Operación (RF-04)",
            new String[]{"Informa conflicto de fechas o exceso de capacidad"}, new Color(220, 38, 38), new Color(220, 38, 38));
        dibujarFlecha(g, 940, 535, 990, 535, "NO (Conflicto)", new Color(220, 38, 38));

        dibujarFlecha(g, 690, 572, 690, 615, "SÍ (Disponible)", SUCCESS);
        dibujarCajaNodo(g, 440, 615, 500, 75, "5. Cálculo Polimórfico y Persistencia Transaccional en MySQL",
            new String[]{"costoTotal = habitacion.calcularCosto(noches, tempAlta) -> INSERT INTO reservas"}, ACCENT, ACCENT);
        dibujarFlecha(g, 690, 690, 690, 730, null, PRIMARY);

        dibujarCajaNodo(g, 480, 730, 420, 65, "6. Actualizar Tablas/Vistas y Cierre de Sesión Controlado (RF-06)",
            new String[]{"Presenta desglose de costos y permite Logout seguro"}, SUCCESS, SUCCESS);

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 4. DIAGRAMA DE CLASES UML MVC (SECCIÓN E.9)
    private static void generarDiagramaClasesMVC(File out) throws Exception {
        BufferedImage img = new BufferedImage(1560, 980, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 4. DIAGRAMA DE CLASES UML - ARQUITECTURA MODELO-VISTA-CONTROLADOR (SECCIÓN 9)",
            "Paquetes: com.hotel.mvc.model, com.hotel.mvc.dao, com.hotel.mvc.controller, com.hotel.mvc.view, com.hotel.mvc.util");

        // Fila 1: Vistas y Controlador
        dibujarCajaClase(g, 30, 95, 320, 175, "<<View>>", "HotelWebServer / index.html", ACCENT,
            new String[]{"- controller: HotelController", "- port: int (8085)", "- server: HttpServer"},
            new String[]{"+ iniciar(): void", "+ handleLogin(HttpExchange): void", "+ handleClientes/Habitaciones(): void", "+ handleDisponibilidad/Reservas(): void"});

        dibujarCajaClase(g, 30, 295, 320, 165, "<<View>>", "SwingHotelView / ConsolaCLIView", ACCENT,
            new String[]{"- controller: HotelController", "- tablas: JTable / Scanner"},
            new String[]{"+ iniciarLoopInteractivo(int): void", "+ construirPanelLogin(): JPanel", "+ recargarTodo(): void"});

        dibujarCajaClase(g, 430, 140, 390, 260, "<<Controller / Facade>>", "HotelController", PRIMARY,
            new String[]{"- conexionMySQL: ConexionMySQL", "- usuarioDAO: UsuarioDAO", "- clienteDAO: ClienteDAO", "- habitacionDAO: HabitacionDAO", "- reservaDAO: ReservaDAO", "- sesionesWeb: Map<String, Usuario>"},
            new String[]{"+ iniciarSesion(user, pass): Usuario", "+ cerrarSesionLocal() / cerrarSesionWeb(): void", "+ registrarCliente(...) / eliminarCliente(...)", "+ registrarHabitacion(...) / cambiarEstado(...)", "+ consultarDisponibilidadPorPeriodo(...): List", "+ registrarReserva(...) / modificarReserva(...)", "+ cancelarReserva(codigo): Reserva", "+ cotizarEstanciaJson(...): String"});

        // Fila Derecha: DAOs y ConexionMySQL
        dibujarCajaClase(g, 900, 95, 300, 165, "<<Singleton JDBC>>", "ConexionMySQL", new Color(71, 85, 105),
            new String[]{"- instance: ConexionMySQL", "- dbName: String = \"hotel_mvc_db\"", "- user: String"},
            new String[]{"+ getInstance(): ConexionMySQL", "+ getConnection(): Connection"});

        dibujarCajaClase(g, 1230, 95, 300, 165, "<<DAO>>", "UsuarioDAO / ClienteDAO", new Color(71, 85, 105),
            new String[]{"- db: ConexionMySQL"},
            new String[]{"+ buscarPorCredencial(String): Optional", "+ insertar(Cliente): boolean", "+ actualizar(Cliente): boolean", "+ eliminar(String): boolean"});

        dibujarCajaClase(g, 900, 285, 630, 165, "<<DAO>>", "HabitacionDAO & ReservaDAO", new Color(71, 85, 105),
            new String[]{"- db: ConexionMySQL"},
            new String[]{"+ insertar(Habitacion) / actualizar(Habitacion) / eliminar(String): boolean", "+ insertar(Reserva) / actualizar(Reserva) / buscarPorCriterio(...): List<Reserva>", "+ buscarConflictosHabitacion(codHab, entrada, salida, excluir): List<Reserva>"});

        // Fila Inferior: Modelo de Dominio (POO)
        dibujarCajaClase(g, 30, 500, 310, 210, "<<Entity>>", "Usuario & Cliente", SUCCESS,
            new String[]{"- codigoCliente: String", "- nombreCompleto: String", "- documento: String", "- telefono / email: String", "- passwordHash / salt: String"},
            new String[]{"+ autenticar(passPlano): boolean", "+ getCodigoCliente(): String", "+ toJson(): String"});

        dibujarCajaClase(g, 380, 500, 390, 255, "<<Entity>>", "Reserva", SUCCESS,
            new String[]{"- codigoReserva: String", "- cliente: Cliente", "- habitacion: Habitacion", "- fechaEntrada / fechaSalida: LocalDate", "- cantidadHuespedes / noches: int", "- temporadaAlta: boolean", "- subtotalBase / cargosAdicionales / costoTotal: double", "- estado: EstadoReserva"},
            new String[]{"+ recalcularImportes(): void", "+ seSuperponeCon(entrada, salida): boolean", "+ actualizarDatosReserva(...): void", "+ cancelar() / completar(): void"});

        dibujarCajaClase(g, 820, 490, 360, 225, "<<Abstract Entity>>", "Habitacion {abstract}", new Color(180, 83, 9),
            new String[]{"- codigoHabitacion: String", "- numero: int", "- capacidad: int", "- precioNoche: double", "- estado: EstadoHabitacion", "- comodidades: String"},
            new String[]{"+ calcularCosto(noches, tempAlta): double {abstract}", "+ getTipo(): String {abstract}", "+ calcularSubtotalBase(noches): double"});

        dibujarCajaClase(g, 820, 765, 225, 165, "<<Subclass>>", "HabitacionSimple", new Color(180, 83, 9),
            new String[]{"+ RECARGO_TA: double = 0.10"},
            new String[]{"+ calcularCosto(n, ta): double", "+ getTipo(): \"SIMPLE\""});

        dibujarCajaClase(g, 1065, 765, 235, 165, "<<Subclass>>", "HabitacionDoble", new Color(180, 83, 9),
            new String[]{"+ DESAYUNO: double = 15.00", "+ RECARGO_TA: double = 0.15"},
            new String[]{"+ calcularCosto(n, ta): double", "+ getTipo(): \"DOBLE\""});

        dibujarCajaClase(g, 1315, 765, 225, 165, "<<Subclass>>", "HabitacionSuite", new Color(180, 83, 9),
            new String[]{"+ PREMIUM: double = 30.00", "+ RECARGO_TA: double = 0.25"},
            new String[]{"+ calcularCosto(n, ta): double", "+ getTipo(): \"SUITE\""});

        // Relaciones principales
        dibujarFlecha(g, 350, 180, 430, 220, "invoca", ACCENT);
        dibujarFlecha(g, 350, 360, 430, 300, "invoca", ACCENT);
        dibujarFlecha(g, 820, 230, 900, 180, "usa", PRIMARY);
        dibujarFlecha(g, 820, 310, 900, 340, "usa", PRIMARY);
        dibujarFlecha(g, 380, 600, 340, 600, "1 Cliente", SUCCESS);
        dibujarFlecha(g, 770, 600, 820, 600, "1 Habitacion", SUCCESS);

        // Herencia hacia Habitacion
        dibujarFlecha(g, 930, 765, 960, 715, "extends", new Color(180, 83, 9));
        dibujarFlecha(g, 1180, 765, 1020, 715, "extends", new Color(180, 83, 9));
        dibujarFlecha(g, 1420, 765, 1100, 715, "extends", new Color(180, 83, 9));

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 5. DIAGRAMA DE SECUENCIA UML
    private static void generarDiagramaSecuencia(File out) throws Exception {
        BufferedImage img = new BufferedImage(1460, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 5. DIAGRAMA DE SECUENCIA UML: REALIZAR RESERVA CON VALIDACIÓN Y PERSISTENCIA MYSQL",
            "Interacción MVC: Vista (Web/Swing/CLI) -> HotelController -> ReservaDAO -> Habitacion (Polimorfismo) -> MySQL");

        String[] lifelines = {
            ":Vista (Web/Swing/CLI)",
            ":HotelController",
            ":ClienteDAO / HabitacionDAO",
            ":ReservaDAO",
            ":Reserva",
            "hab:Habitacion (Subclase)",
            "MySQL (hotel_mvc_db)"
        };
        int[] lx = {110, 320, 540, 760, 960, 1160, 1360};

        for (int i = 0; i < lifelines.length; i++) {
            dibujarCajaNodo(g, lx[i] - 85, 95, 170, 48, lifelines[i], new String[]{}, PRIMARY, PRIMARY);
            g.setColor(BORDER);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
            g.drawLine(lx[i], 143, lx[i], 780);
        }

        int y = 185;
        dibujarFlecha(g, lx[0], y, lx[1], y, "1: registrarReserva(codCli, codHab, entrada, salida, huesp, tempAlta)", PRIMARY);
        y += 50;
        dibujarFlecha(g, lx[1], y, lx[2], y, "2: buscarPorCodigo(codCli) y buscarPorCodigo(codHab)", ACCENT);
        y += 45;
        dibujarFlecha(g, lx[2], y, lx[6], y, "3: SELECT * FROM clientes / habitaciones WHERE codigo = ?", new Color(71, 85, 105));
        y += 50;
        dibujarFlecha(g, lx[1], y, lx[3], y, "4: buscarConflictosHabitacion(codHab, entrada, salida)", ACCENT);
        y += 45;
        dibujarFlecha(g, lx[3], y, lx[6], y, "5: SELECT * FROM reservas WHERE estado='CONFIRMADA' AND entrada < ? AND salida > ?", new Color(71, 85, 105));
        y += 50;
        dibujarFlecha(g, lx[1], y, lx[4], y, "6: <<create>> new Reserva(codRes, cliente, hab, entrada, salida, huesp, tempAlta)", SUCCESS);
        y += 50;
        dibujarFlecha(g, lx[4], y, lx[5], y, "7: calcularCosto(noches, tempAlta) [Despacho Polimórfico Dinámico]", WARNING);
        y += 45;
        dibujarFlecha(g, lx[5], y, lx[4], y, "8: retorna costoTotal calculado según subclase (Simple/Doble/Suite)", WARNING);
        y += 50;
        dibujarFlecha(g, lx[1], y, lx[3], y, "9: insertar(reserva)", SUCCESS);
        y += 45;
        dibujarFlecha(g, lx[3], y, lx[6], y, "10: INSERT INTO reservas (...) VALUES (...)", new Color(71, 85, 105));
        y += 50;
        dibujarFlecha(g, lx[1], y, lx[2], y, "11: cambiarEstado(codHab, EstadoHabitacion.OCUPADA)", ACCENT);
        y += 45;
        dibujarFlecha(g, lx[1], y, lx[0], y, "12: retorna Reserva confirmada con desglose de costos", PRIMARY);

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 6. DIAGRAMA DE ESTADOS UML
    private static void generarDiagramaEstados(File out) throws Exception {
        BufferedImage img = new BufferedImage(1320, 740, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 6. DIAGRAMA DE ESTADOS UML: CICLO DE VIDA DE RESERVA Y HABITACIÓN",
            "Validación estricta de transiciones (Corrige hallazgo de cancelación sobre reservas completadas)");

        // Máquina 1: EstadoReserva
        g.setColor(PRIMARY);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("A) Máquina de Estados de la Entidad Reserva (EstadoReserva)", 60, 120);

        g.fillOval(80, 210, 26, 26);
        dibujarFlecha(g, 106, 223, 220, 223, "new Reserva(...)", PRIMARY);

        dibujarCajaNodo(g, 220, 180, 280, 90, "ESTADO: CONFIRMADA",
            new String[]{"entry / hab.calcularCosto()", "do / Bloquea período [entrada, salida)"}, SUCCESS, SUCCESS);

        dibujarCajaNodo(g, 720, 115, 310, 90, "ESTADO: COMPLETADA",
            new String[]{"Check-out finalizado satisfactoriamente", "No admite cancelación posterior"}, ACCENT, ACCENT);

        dibujarCajaNodo(g, 720, 265, 310, 90, "ESTADO: CANCELADA",
            new String[]{"Reserva anulada por solicitud del huésped", "Libera disponibilidad del período"}, new Color(220, 38, 38), new Color(220, 38, 38));

        dibujarFlecha(g, 500, 205, 720, 160, "completar() [estado == CONFIRMADA]", ACCENT);
        dibujarFlecha(g, 500, 245, 720, 310, "cancelar() [estado == CONFIRMADA]", new Color(220, 38, 38));

        // Máquina 2: EstadoHabitacion
        g.setColor(PRIMARY);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("B) Máquina de Estados de la Entidad Habitación (EstadoHabitacion)", 60, 440);

        dibujarCajaNodo(g, 140, 500, 260, 90, "DISPONIBLE",
            new String[]{"Habilitada para nuevas reservas", "Sin ocupación activa"}, SUCCESS, SUCCESS);

        dibujarCajaNodo(g, 530, 500, 260, 90, "OCUPADA",
            new String[]{"Con reserva CONFIRMADA activa", "Se libera al cancelar/completar"}, WARNING, WARNING);

        dibujarCajaNodo(g, 920, 500, 280, 90, "FUERA_DE_SERVICIO",
            new String[]{"En mantenimiento técnico", "Excluida en Proceso 3 y Proceso 4"}, new Color(220, 38, 38), new Color(220, 38, 38));

        dibujarFlecha(g, 400, 530, 530, 530, "registrarReserva()", WARNING);
        dibujarFlecha(g, 530, 565, 400, 565, "cancelar() / completar()", SUCCESS);
        dibujarFlecha(g, 400, 500, 920, 500, "cambiarEstado(FUERA_DE_SERVICIO)", new Color(220, 38, 38));

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 7. DIAGRAMA ENTIDAD-RELACIÓN CONCEPTUAL (NOTACIÓN CHEN / PATA DE GALLO)
    private static void generarDiagramaERConceptual(File out) throws Exception {
        BufferedImage img = new BufferedImage(1440, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 7. DIAGRAMA ENTIDAD-RELACIÓN (E-R CONCEPTUAL) - BASE DE DATOS HOTEL_MVC_DB",
            "Entidades, Atributos Clave y Cardinalidades del Dominio Hotelero (Sección 10)");

        dibujarCajaNodo(g, 70, 140, 300, 170, "ENTIDAD: USUARIO",
            new String[]{
                "PK: id_usuario",
                "• username (Único)",
                "• email (Único)",
                "• nombre_completo",
                "• password_hash (SHA-256) + salt",
                "• rol, activo"
            }, PRIMARY, PRIMARY);

        dibujarCajaNodo(g, 70, 460, 300, 180, "ENTIDAD: CLIENTE",
            new String[]{
                "PK: codigo_cliente",
                "• nombre_completo",
                "• documento (DUI/DNI Único)",
                "• telefono",
                "• email",
                "• activo, fecha_registro"
            }, SUCCESS, SUCCESS);

        dibujarCajaNodo(g, 1050, 460, 320, 195, "ENTIDAD: HABITACION",
            new String[]{
                "PK: codigo_habitacion",
                "• numero (Único)",
                "• tipo (SIMPLE | DOBLE | SUITE)",
                "• capacidad",
                "• precio_noche",
                "• estado (DISPONIBLE | OCUPADA | FUERA)",
                "• comodidades, cargo_extra"
            }, WARNING, WARNING);

        dibujarCajaNodo(g, 530, 430, 360, 250, "ENTIDAD ASOCIATIVA: RESERVA",
            new String[]{
                "PK: codigo_reserva",
                "FK1: codigo_cliente (ref. CLIENTE)",
                "FK2: codigo_habitacion (ref. HABITACION)",
                "• fecha_entrada, fecha_salida",
                "• cantidad_huespedes, noches",
                "• temporada_alta",
                "• tarifa_base_noche, subtotal_base",
                "• cargos_adicionales, costo_total",
                "• estado (CONFIRMADA | CANCELADA | COMPLETADA)"
            }, ACCENT, ACCENT);

        // Rombo de relación Cliente - Reserva
        dibujarFlecha(g, 370, 550, 530, 550, "1 : N (Realiza)", SUCCESS);
        // Rombo de relación Habitacion - Reserva
        dibujarFlecha(g, 1050, 550, 890, 550, "1 : N (Aloja en)", WARNING);
        // Relación de control Usuario -> Reserva
        dibujarFlecha(g, 220, 310, 620, 430, "Autentica y Gestiona (RF-01)", PRIMARY);

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 8. DIAGRAMA LÓGICO RELACIONAL (ESQUEMA NORMALIZADO 3FN)
    private static void generarDiagramaLogicoMySQL(File out) throws Exception {
        BufferedImage img = new BufferedImage(1460, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 8. DIAGRAMA LÓGICO RELACIONAL (3FN) - ESQUEMA NORMALIZADO PARA MYSQL",
            "Definición de Claves Primarias (PK), Claves Foráneas (FK), Índices y Restricciones de Unicidad (UK)");

        dibujarCajaClase(g, 60, 130, 340, 230, "<<Tabla Lógica>>", "USUARIOS", PRIMARY,
            new String[]{
                "PK id_usuario : VARCHAR(20) NOT NULL",
                "UK username : VARCHAR(50) NOT NULL",
                "UK email : VARCHAR(100) NOT NULL",
                "   nombre_completo : VARCHAR(100) NOT NULL",
                "   password_hash : VARCHAR(64) NOT NULL",
                "   salt : VARCHAR(32) NOT NULL",
                "   rol : VARCHAR(30) NOT NULL",
                "   activo : BOOLEAN NOT NULL"
            },
            new String[]{"UQ_usuarios_username (username)", "UQ_usuarios_email (email)"});

        dibujarCajaClase(g, 60, 450, 340, 230, "<<Tabla Lógica>>", "CLIENTES", SUCCESS,
            new String[]{
                "PK codigo_cliente : VARCHAR(20) NOT NULL",
                "   nombre_completo : VARCHAR(100) NOT NULL",
                "UK documento : VARCHAR(25) NOT NULL",
                "   telefono : VARCHAR(25) NOT NULL",
                "   email : VARCHAR(100) NOT NULL",
                "   activo : BOOLEAN NOT NULL",
                "   fecha_registro : TIMESTAMP"
            },
            new String[]{"UQ_clientes_documento (documento)", "IDX_clientes_busqueda (nombre, doc)"});

        dibujarCajaClase(g, 1040, 440, 360, 250, "<<Tabla Lógica>>", "HABITACIONES", WARNING,
            new String[]{
                "PK codigo_habitacion : VARCHAR(20) NOT NULL",
                "UK numero : INT NOT NULL",
                "   tipo : VARCHAR(20) NOT NULL",
                "   capacidad : INT NOT NULL",
                "   precio_noche : DECIMAL(10,2) NOT NULL",
                "   estado : VARCHAR(25) NOT NULL",
                "   comodidades : VARCHAR(255) NOT NULL",
                "   cargo_extra : DECIMAL(10,2) NOT NULL"
            },
            new String[]{"UQ_habitaciones_numero (numero)", "CHK_capacidad_precio (capacidad>0, precio>0)"});

        dibujarCajaClase(g, 490, 360, 440, 350, "<<Tabla Lógica Transaccional>>", "RESERVAS", ACCENT,
            new String[]{
                "PK codigo_reserva : VARCHAR(20) NOT NULL",
                "FK codigo_cliente : VARCHAR(20) NOT NULL",
                "FK codigo_habitacion : VARCHAR(20) NOT NULL",
                "   fecha_entrada : DATE NOT NULL",
                "   fecha_salida : DATE NOT NULL",
                "   cantidad_huespedes : INT NOT NULL",
                "   temporada_alta : BOOLEAN NOT NULL",
                "   noches : INT NOT NULL",
                "   tarifa_base_noche : DECIMAL(10,2) NOT NULL",
                "   subtotal_base : DECIMAL(10,2) NOT NULL",
                "   cargos_adicionales : DECIMAL(10,2) NOT NULL",
                "   costo_total : DECIMAL(10,2) NOT NULL",
                "   estado : VARCHAR(20) NOT NULL"
            },
            new String[]{
                "FK_reserva_cliente -> CLIENTES(codigo_cliente)",
                "FK_reserva_habitacion -> HABITACIONES(codigo_habitacion)",
                "CHK_fechas_validas (fecha_salida > fecha_entrada)"
            });

        dibujarFlecha(g, 490, 540, 400, 540, "FK (N:1) RESTRICT", SUCCESS);
        dibujarFlecha(g, 930, 540, 1040, 540, "FK (N:1) RESTRICT", WARNING);

        g.dispose();
        ImageIO.write(img, "png", out);
    }

    // 9. DIAGRAMA FÍSICO MYSQL 8.0 (ENGINE=InnoDB, UTF8MB4, CONSTRAINTS)
    private static void generarDiagramaFisicoMySQL(File out) throws Exception {
        BufferedImage img = new BufferedImage(1460, 840, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = crearLienzo(img,
            "FIGURA 9. DIAGRAMA FÍSICO DE BASE DE DATOS MYSQL 8.0 (ENGINE=InnoDB, CHARSET=utf8mb4)",
            "Especificación Física DDL de Tablas, Tipos de Columna, Restricciones ON DELETE RESTRICT e Índices B-Tree");

        dibujarCajaClase(g, 40, 110, 660, 310, "<<Physical Table: hotel_mvc_db.clientes & usuarios>>",
            "DDL Físico: Tablas Maestras (usuarios / clientes)", PRIMARY,
            new String[]{
                "CREATE TABLE usuarios (id_usuario VARCHAR(20) PRIMARY KEY,",
                "  username VARCHAR(50) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE,",
                "  password_hash VARCHAR(64) NOT NULL, salt VARCHAR(32) NOT NULL,",
                "  rol VARCHAR(30) DEFAULT 'RECEPCIONISTA', activo TINYINT(1) DEFAULT 1) ENGINE=InnoDB;",
                "------------------------------------------------------------------------",
                "CREATE TABLE clientes (codigo_cliente VARCHAR(20) PRIMARY KEY,",
                "  nombre_completo VARCHAR(100) NOT NULL, documento VARCHAR(25) NOT NULL UNIQUE,",
                "  telefono VARCHAR(25) NOT NULL, email VARCHAR(100) NOT NULL,",
                "  activo TINYINT(1) NOT NULL DEFAULT 1,",
                "  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;"
            },
            new String[]{"INDEX idx_clientes_busqueda (nombre_completo, documento) USING BTREE"});

        dibujarCajaClase(g, 750, 110, 670, 310, "<<Physical Table: hotel_mvc_db.habitaciones>>",
            "DDL Físico: Tabla Polimórfica habitaciones", WARNING,
            new String[]{
                "CREATE TABLE habitaciones (",
                "  codigo_habitacion VARCHAR(20) PRIMARY KEY,",
                "  numero INT NOT NULL UNIQUE,",
                "  tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('SIMPLE','DOBLE','SUITE')),",
                "  capacidad INT NOT NULL CHECK (capacidad > 0),",
                "  precio_noche DECIMAL(10,2) NOT NULL CHECK (precio_noche > 0),",
                "  estado VARCHAR(25) NOT NULL DEFAULT 'DISPONIBLE',",
                "  comodidades VARCHAR(255) NOT NULL,",
                "  cargo_extra DECIMAL(10,2) NOT NULL DEFAULT 0.00",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
            },
            new String[]{"UNIQUE KEY uk_numero (numero) USING BTREE"});

        dibujarCajaClase(g, 180, 460, 1100, 340, "<<Physical Table: hotel_mvc_db.reservas>>",
            "DDL Físico: Tabla Transaccional reservas (Integridad Referencial InnoDB)", ACCENT,
            new String[]{
                "CREATE TABLE reservas (",
                "  codigo_reserva VARCHAR(20) PRIMARY KEY,",
                "  codigo_cliente VARCHAR(20) NOT NULL,  codigo_habitacion VARCHAR(20) NOT NULL,",
                "  fecha_entrada DATE NOT NULL,          fecha_salida DATE NOT NULL,",
                "  cantidad_huespedes INT NOT NULL CHECK (cantidad_huespedes > 0),",
                "  temporada_alta TINYINT(1) NOT NULL DEFAULT 0, noches INT NOT NULL CHECK (noches > 0),",
                "  tarifa_base_noche DECIMAL(10,2) NOT NULL, subtotal_base DECIMAL(10,2) NOT NULL,",
                "  cargos_adicionales DECIMAL(10,2) NOT NULL DEFAULT 0.00, costo_total DECIMAL(10,2) NOT NULL,",
                "  estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',",
                "  CONSTRAINT chk_fechas_validas CHECK (fecha_salida > fecha_entrada),",
                "  CONSTRAINT fk_reserva_cliente FOREIGN KEY (codigo_cliente) REFERENCES clientes(codigo_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,",
                "  CONSTRAINT fk_reserva_habitacion FOREIGN KEY (codigo_habitacion) REFERENCES habitaciones(codigo_habitacion) ON UPDATE CASCADE ON DELETE RESTRICT",
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
            },
            new String[]{"INDEX idx_reservas_fechas (codigo_habitacion, estado, fecha_entrada, fecha_salida) USING BTREE"});

        dibujarFlecha(g, 420, 460, 380, 420, "ON DELETE RESTRICT", PRIMARY);
        dibujarFlecha(g, 1020, 460, 1060, 420, "ON DELETE RESTRICT", WARNING);

        g.dispose();
        ImageIO.write(img, "png", out);
    }
}
