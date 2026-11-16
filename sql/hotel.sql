-- Base de datos del Sistema de Hotel 
-- Ejecutar con el usuario root de su MySQL (Workbench o consola)
CREATE DATABASE IF NOT EXISTS hotel CHARACTER SET utf8mb4;
USE hotel;

-- Usuario de la aplicación 
CREATE USER IF NOT EXISTS 'hotel_user'@'localhost' IDENTIFIED BY 'hotel123';
GRANT ALL PRIVILEGES ON hotel.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(30) NOT NULL UNIQUE,
    clave VARCHAR(64) NOT NULL            -- se guarda cifrada con SHA-256
);

CREATE TABLE IF NOT EXISTS habitaciones (
    numero INT PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    precio DECIMAL(8,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Disponible'
);

CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    dui VARCHAR(10) NOT NULL UNIQUE,
    telefono VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS reservas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    num_habitacion INT NOT NULL,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id),
    FOREIGN KEY (num_habitacion) REFERENCES habitaciones(numero)
);

-- Datos de prueba
INSERT INTO usuarios (usuario, clave) VALUES ('admin', SHA2('admin123', 256));

INSERT INTO habitaciones (numero, tipo, precio) VALUES
    (101, 'Sencilla', 35.00),
    (102, 'Doble', 55.00),
    (201, 'Suite', 90.00);

INSERT INTO clientes (nombre, dui, telefono) VALUES
    ('Ana Martínez', '01234567-8', '7777-1111'),
    ('Carlos López', '07654321-0', '7777-2222');
