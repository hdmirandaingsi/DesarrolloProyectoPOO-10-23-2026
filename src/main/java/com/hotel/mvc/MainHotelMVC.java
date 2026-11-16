package com.hotel.mvc;

import com.hotel.mvc.controller.HotelController;
import com.hotel.mvc.controller.HotelWebServer;
import com.hotel.mvc.view.ConsolaCLIView;
import com.hotel.mvc.view.SwingHotelView;
import java.io.File;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal del Sistema de Reservas del Hotel "Gran Sol & Montaña" (Clave 1).
 * Soporta ejecución desde CLI (terminal) en tres modos sincronizados contra MySQL 8.0:
 *   --web   : Inicia únicamente el Servidor Web HTTP del JDK (http://localhost:8085)
 *   --swing : Inicia el Servidor Web HTTP + la Interfaz Gráfica Java Swing
 *   --cli   : (Por defecto) Inicia el Servidor Web HTTP + el Menú Interactivo en Consola CLI
 */
public class MainHotelMVC {

    public static final int DEFAULT_PORT = 8085;

    public static void main(String[] args) throws Exception {
        String modo = "cli";
        int port = DEFAULT_PORT;

        for (String arg : args) {
            if ("--web".equalsIgnoreCase(arg)) {
                modo = "web";
            } else if ("--swing".equalsIgnoreCase(arg)) {
                modo = "swing";
            } else if ("--cli".equalsIgnoreCase(arg)) {
                modo = "cli";
            } else if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring("--port=".length()));
            }
        }

        HotelController controller = new HotelController();
        File webDir = localizarDirectorioWeb();
        HotelWebServer webServer = new HotelWebServer(controller, port, webDir);
        webServer.iniciar();

        System.out.println("[SERVIDOR WEB JDK] Activo en: http://localhost:" + port + " (Sirviendo desde: " + webDir.getAbsolutePath() + ")");
        System.out.println("[BASE DE DATOS]    MySQL usa la configuración local proporcionada por el operador.");

        if ("swing".equalsIgnoreCase(modo)) {
            SwingUtilities.invokeLater(() -> {
                SwingHotelView view = new SwingHotelView(controller);
                view.setVisible(true);
            });
        } else if ("web".equalsIgnoreCase(modo)) {
            System.out.println("[MODO DAEMON WEB] Servidor escuchando peticiones HTTP. Presione Ctrl+C para detener.");
            Thread.currentThread().join();
        } else {
            ConsolaCLIView cli = new ConsolaCLIView(controller);
            cli.iniciarLoopInteractivo(port);
            webServer.detener();
        }
    }

    private static File localizarDirectorioWeb() {
        File f1 = new File("web");
        if (f1.exists() && f1.isDirectory()) {
            return f1;
        }
        return f1;
    }
}
