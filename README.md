# Sistema de Hotel – Proyecto POO (Java 11 + Swing + MySQL)

Arquitectura **MVC**:

| Paquete | Responsable | Contenido |
|---|---|---|
| `modelo` | hdmirandaingsi | Entidades, interfaces `I...DAO` y las clases DAO con JDBC |
| `vista` | Kevin | Formularios Java Swing (login, menú, módulos) |
| `controlador` | René | Validaciones y lógica entre la vista y el modelo |
| `util` | René | `Conexion` (JDBC) |

## Cómo empezar (todos)

1. Tener **MySQL** corriendo en su PC (MySQL Server + Workbench, o XAMPP).
2. Con el usuario **root**, ejecutar todo el contenido de `sql/hotel.sql`
   (crea la base `hotel`, el usuario `hotel_user` / `hotel123`, las tablas y datos de prueba).
3. Abrir el proyecto en NetBeans 22 (Maven, **JDK 11**) y hacer *Build with Dependencies*.
4. Ejecutar `util/Conexion.java`: debe imprimir **Conexión exitosa**.
5. Ejecutar `pruebas/PruebaControladores.java`: todas las pruebas deben pasar.

Usuario de prueba para el login: **admin / admin123**.

## Para hdmirandaingsi (Modelo)

Cada DAO debe **implementar** su interfaz (ya está en `modelo`), usando
`PreparedStatement` y `Conexion.getConexion()`:

```java
public class HabitacionDAO implements IHabitacionDAO { ... }
public class ClienteDAO    implements IClienteDAO    { ... }
public class ReservaDAO    implements IReservaDAO    { ... }
public class UsuarioDAO    implements IUsuarioDAO    { ... }
```

Las clases `Habitacion`, `Cliente` y `Reserva` son una base: pueden ampliarse,
pero sin quitar los métodos que usan los controladores.

## Para Kevin (Vista)

Crear los controladores pasándoles los DAO de hdmirandaingsi:

```java
HabitacionControlador hc = new HabitacionControlador(new HabitacionDAO());
ClienteControlador cc = new ClienteControlador(new ClienteDAO());
ReservaControlador rc = new ReservaControlador(new ReservaDAO(), new ClienteDAO(), new HabitacionDAO());
LoginControlador lc = new LoginControlador(new UsuarioDAO());
```

Los métodos que guardan/modifican devuelven **"OK"** o un mensaje de error:

```java
String r = hc.guardar(txtNumero.getText(), txtTipo.getText(), txtPrecio.getText());
if (r.equals("OK")) {
    JOptionPane.showMessageDialog(this, "Guardado correctamente");
} else {
    JOptionPane.showMessageDialog(this, r, "Error", JOptionPane.ERROR_MESSAGE);
}
```

Las fechas de reserva se envían como texto `dd/MM/yyyy` (ej. `20/11/2026`).

## Reglas del equipo

- Cada integrante trabaja en su rama y hace commits pequeños desde su cuenta personal.
- `git pull` antes de empezar y `git push` al terminar cada pieza.
- El proyecto compila con Java 11 (`maven.compiler.source/target` en `pom.xml`).
- Si la conexión falla: MySQL no está iniciado, no se ejecutó `sql/hotel.sql`, o el usuario/clave de `Conexion.java` no coinciden.
