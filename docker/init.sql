-- Script de inicialización de la base de datos
-- Se ejecuta automáticamente al levantar el contenedor MySQL por primera vez

SET NAMES utf8mb4;
SET time_zone = 'America/El_Salvador';

-- Crear la base de datos si no existe (ya la crea Docker, pero por seguridad)
CREATE DATABASE IF NOT EXISTS planillas
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE planillas;

-- Los demás DDL los genera Hibernate con ddl-auto=update
-- Este script solo garantiza la base de datos y el charset correcto
