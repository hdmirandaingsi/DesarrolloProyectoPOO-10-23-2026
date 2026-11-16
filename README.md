# Sistema de reservas de hotel

Aplicación Java 11 para administrar clientes, habitaciones y reservas. Sigue una arquitectura MVC y ofrece interfaces web, Swing y CLI, con persistencia MySQL mediante JDBC. Las tarifas se calculan polimórficamente para habitaciones simples, dobles y suites.

Esta versión consolida los archivos del proyecto y los organiza en la estructura Maven del repositorio. Reconoce y conserva el trabajo previo del equipo como parte de su historial; la reorganización busca facilitar la compilación, la configuración local y el mantenimiento, sin atribuir aportes individuales cuando no es posible verificarlos en cada archivo.

## Requisitos

- JDK 11
- Maven 3.8 o posterior
- MySQL 8.0 o posterior

## Compilación

Desde la raíz del repositorio:

```shell
mvn compile
```

La dependencia MySQL Connector/J 8.3.0 se descarga desde Maven; no se necesita guardar el JAR en el repositorio.

## Configuración de MySQL

1. Cree una copia local de la plantilla:

   ```shell
   copy db.properties.example db.properties
   ```

2. Edite `db.properties` con los datos de conexión de su entorno. Este archivo está excluido de Git. También se aceptan variables `HOTEL_DB_HOST`, `HOTEL_DB_PORT`, `HOTEL_DB_NAME`, `HOTEL_DB_USER` y `HOTEL_DB_PASS`, o propiedades Java `hotel.db.host`, `hotel.db.port`, `hotel.db.name`, `hotel.db.user` y `hotel.db.pass`. Los valores de propiedades Java tienen prioridad, luego las variables de entorno y, por último, `db.properties`.
3. Cree el esquema con una cuenta MySQL autorizada:

   ```shell
   mysql -u SU_USUARIO -p < sql/init_hotel_mvc.sql
   ```

   El script crea la base y tablas faltantes; no elimina datos ni crea cuentas MySQL. La aplicación tampoco prueba usuarios alternativos ni crea el esquema al arrancar.
4. Configure una cuenta de aplicación en la tabla `usuarios` mediante el procedimiento seguro aprobado para su entorno. No se incluyen cuentas ni contraseñas de demostración. Las contraseñas deben almacenarse como hash y salt compatibles con `SecurityAndValidationUtil`.

Inicie la aplicación desde la raíz del repositorio:

```shell
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.hotel.mvc.MainHotelMVC
```

El modo predeterminado abre el servidor web y el menú CLI. Use `-Dexec.args=--web` para solo web, `-Dexec.args=--swing` para Swing o `-Dexec.args=--port=PUERTO` para cambiar el puerto. La vista web se sirve en `http://localhost:8085`.

## Estructura principal

- `src/main/java/com/hotel/mvc/model`: entidades, estados, reglas de dominio y fábrica de habitaciones.
- `src/main/java/com/hotel/mvc/dao`: acceso JDBC y consultas parametrizadas.
- `src/main/java/com/hotel/mvc/controller`: coordinación MVC y servidor HTTP.
- `src/main/java/com/hotel/mvc/view`: vistas CLI y Swing.
- `src/main/java/com/hotel/mvc/test`: suite de pruebas integrales existente.
- `src/main/java/com/hotel/mvc/util`: validaciones, hash de contraseñas y generador de diagramas UML.
- `web`: interfaz web estática.
- `sql/init_hotel_mvc.sql`: esquema inicial no destructivo, sin datos personales ni cuentas de prueba.

La suite `PruebasIntegralesMVC` requiere una base configurada y contiene verificaciones de integración con persistencia; revise sus precondiciones antes de ejecutarla.
