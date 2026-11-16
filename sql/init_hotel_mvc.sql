-- Esquema inicial no destructivo para el sistema de reservas.
-- No elimina bases/tablas ni inserta credenciales o datos personales de ejemplo.
CREATE DATABASE IF NOT EXISTS hotel_mvc_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE hotel_mvc_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    salt VARCHAR(32) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'RECEPCIONISTA',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
    codigo_cliente VARCHAR(20) PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    documento VARCHAR(25) NOT NULL UNIQUE,
    telefono VARCHAR(25) NOT NULL,
    email VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS habitaciones (
    codigo_habitacion VARCHAR(20) PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('SIMPLE', 'DOBLE', 'SUITE')),
    capacidad INT NOT NULL CHECK (capacidad > 0),
    precio_noche DECIMAL(10,2) NOT NULL CHECK (precio_noche > 0),
    estado VARCHAR(25) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'FUERA_DE_SERVICIO')),
    comodidades VARCHAR(255) NOT NULL,
    cargo_extra DECIMAL(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservas (
    codigo_reserva VARCHAR(20) PRIMARY KEY,
    codigo_cliente VARCHAR(20) NOT NULL,
    codigo_habitacion VARCHAR(20) NOT NULL,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    cantidad_huespedes INT NOT NULL CHECK (cantidad_huespedes > 0),
    temporada_alta BOOLEAN NOT NULL DEFAULT FALSE,
    noches INT NOT NULL CHECK (noches > 0),
    tarifa_base_noche DECIMAL(10,2) NOT NULL,
    subtotal_base DECIMAL(10,2) NOT NULL,
    cargos_adicionales DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    costo_total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA'
        CHECK (estado IN ('CONFIRMADA', 'CANCELADA', 'COMPLETADA')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fechas_validas CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (codigo_cliente)
        REFERENCES clientes(codigo_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_habitacion FOREIGN KEY (codigo_habitacion)
        REFERENCES habitaciones(codigo_habitacion) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;
