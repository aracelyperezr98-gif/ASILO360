-- ============================================================
-- BASE DE DATOS: asilo360
-- Sistema de Gestión para Asilo y Residencia de Adultos Mayores
-- Codificación: utf8mb4
-- Motor: InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS `asilo360`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `asilo360`;

-- ------------------------------------------------------------
-- 1. TABLA: roles
-- Almacena los roles de acceso del sistema.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `roles` (
    `id_rol` INT PRIMARY KEY AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar roles iniciales del sistema
INSERT INTO `roles` (`id_rol`, `nombre`) VALUES
(1, 'Administrador'),
(2, 'Personal'),
(3, 'Familiar')
ON DUPLICATE KEY UPDATE `nombre` = VALUES(`nombre`);

-- ------------------------------------------------------------
-- 2. TABLA: usuarios
-- Cuenta general de acceso al sistema con contraseña hasheada.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `usuarios` (
    `id_usuario` INT PRIMARY KEY AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `apellido_paterno` VARCHAR(100) NOT NULL,
    `apellido_materno` VARCHAR(100),
    `correo` VARCHAR(150) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `id_rol` INT NOT NULL,
    `telefono` VARCHAR(20),
    `estado` BOOLEAN DEFAULT TRUE,
    `fecha_registro` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_usuarios_roles` FOREIGN KEY (`id_rol`)
        REFERENCES `roles` (`id_rol`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_usuarios_correo` (`correo`),
    INDEX `idx_usuarios_id_rol` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. TABLA: residentes
-- Información de los adultos mayores residentes en el asilo.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `residentes` (
    `id_residente` INT PRIMARY KEY AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `apellido_paterno` VARCHAR(100) NOT NULL,
    `apellido_materno` VARCHAR(100),
    `fecha_nacimiento` DATE,
    `sexo` VARCHAR(20),
    `habitacion` VARCHAR(20),
    `fecha_ingreso` DATE,
    `estado` VARCHAR(30) DEFAULT 'Activo',
    `observaciones` TEXT,
    INDEX `idx_residentes_nombre` (`nombre`, `apellido_paterno`),
    INDEX `idx_residentes_habitacion` (`habitacion`),
    INDEX `idx_residentes_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. TABLA: familiares
-- Información complementaria de usuarios tipo Familiar.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `familiares` (
    `id_familiar` INT PRIMARY KEY AUTO_INCREMENT,
    `id_usuario` INT NOT NULL,
    `parentesco` VARCHAR(50),
    `telefono` VARCHAR(20),
    CONSTRAINT `fk_familiares_usuarios` FOREIGN KEY (`id_usuario`)
        REFERENCES `usuarios` (`id_usuario`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_familiares_id_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. TABLA: residente_familiar
-- Tabla intermedia N:M entre residentes y sus familiares.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `residente_familiar` (
    `id_residente` INT NOT NULL,
    `id_familiar` INT NOT NULL,
    `parentesco` VARCHAR(50),
    PRIMARY KEY (`id_residente`, `id_familiar`),
    CONSTRAINT `fk_resfam_residente` FOREIGN KEY (`id_residente`)
        REFERENCES `residentes` (`id_residente`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_resfam_familiar` FOREIGN KEY (`id_familiar`)
        REFERENCES `familiares` (`id_familiar`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_resfam_familiar` (`id_familiar`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 6. TABLA: personal
-- Información del personal médico, enfermería y administrativo.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `personal` (
    `id_personal` INT PRIMARY KEY AUTO_INCREMENT,
    `id_usuario` INT NOT NULL,
    `puesto` VARCHAR(100),
    `fecha_ingreso` DATE,
    `estado` BOOLEAN DEFAULT TRUE,
    CONSTRAINT `fk_personal_usuarios` FOREIGN KEY (`id_usuario`)
        REFERENCES `usuarios` (`id_usuario`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_personal_id_usuario` (`id_usuario`),
    INDEX `idx_personal_puesto` (`puesto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 7. TABLA: consultas
-- Consultas médicas y chequeos realizados a los residentes.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `consultas` (
    `id_consulta` INT PRIMARY KEY AUTO_INCREMENT,
    `id_residente` INT NOT NULL,
    `id_personal` INT NOT NULL,
    `fecha` DATE NOT NULL,
    `hora` TIME,
    `motivo` VARCHAR(255),
    `observaciones` TEXT,
    `estado` VARCHAR(30) DEFAULT 'Completada',
    CONSTRAINT `fk_consultas_residente` FOREIGN KEY (`id_residente`)
        REFERENCES `residentes` (`id_residente`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_consultas_personal` FOREIGN KEY (`id_personal`)
        REFERENCES `personal` (`id_personal`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_consultas_residente` (`id_residente`),
    INDEX `idx_consultas_personal` (`id_personal`),
    INDEX `idx_consultas_fecha` (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 8. TABLA: visitas
-- Registro de visitas de familiares a los residentes.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `visitas` (
    `id_visita` INT PRIMARY KEY AUTO_INCREMENT,
    `id_residente` INT NOT NULL,
    `id_familiar` INT NOT NULL,
    `fecha` DATE NOT NULL,
    `hora` TIME,
    `numero_visitantes` INT DEFAULT 1,
    `estado` VARCHAR(30) DEFAULT 'Programada',
    CONSTRAINT `fk_visitas_residente` FOREIGN KEY (`id_residente`)
        REFERENCES `residentes` (`id_residente`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_visitas_familiar` FOREIGN KEY (`id_familiar`)
        REFERENCES `familiares` (`id_familiar`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_visitas_residente` (`id_residente`),
    INDEX `idx_visitas_familiar` (`id_familiar`),
    INDEX `idx_visitas_fecha` (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 9. TABLA: pagos
-- Registro de cuotas y servicios pagados por el residente/familiar.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `pagos` (
    `id_pago` INT PRIMARY KEY AUTO_INCREMENT,
    `id_residente` INT NOT NULL,
    `monto` DECIMAL(10,2) NOT NULL,
    `fecha_pago` DATE NOT NULL,
    `concepto` VARCHAR(255),
    `metodo_pago` VARCHAR(50),
    `estado` VARCHAR(30) DEFAULT 'Pagado',
    CONSTRAINT `fk_pagos_residente` FOREIGN KEY (`id_residente`)
        REFERENCES `residentes` (`id_residente`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_pagos_residente` (`id_residente`),
    INDEX `idx_pagos_fecha_pago` (`fecha_pago`),
    INDEX `idx_pagos_estado` (`estado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 10. TABLA: actividades
-- Registro de actividades de cuidado y recreativas de residentes.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `actividades` (
    `id_actividad` INT PRIMARY KEY AUTO_INCREMENT,
    `id_residente` INT NOT NULL,
    `id_personal` INT NOT NULL,
    `fecha` DATE NOT NULL,
    `hora` TIME,
    `tipo` VARCHAR(100),
    `descripcion` TEXT,
    CONSTRAINT `fk_actividades_residente` FOREIGN KEY (`id_residente`)
        REFERENCES `residentes` (`id_residente`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_actividades_personal` FOREIGN KEY (`id_personal`)
        REFERENCES `personal` (`id_personal`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_actividades_residente` (`id_residente`),
    INDEX `idx_actividades_personal` (`id_personal`),
    INDEX `idx_actividades_fecha` (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 11. TABLA: notificaciones
-- Notificaciones y alertas enviadas a los usuarios del sistema.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notificaciones` (
    `id_notificacion` INT PRIMARY KEY AUTO_INCREMENT,
    `id_usuario` INT NOT NULL,
    `titulo` VARCHAR(150),
    `mensaje` TEXT,
    `tipo` VARCHAR(50),
    `leida` BOOLEAN DEFAULT FALSE,
    `fecha` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notificaciones_usuario` FOREIGN KEY (`id_usuario`)
        REFERENCES `usuarios` (`id_usuario`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_notificaciones_usuario` (`id_usuario`),
    INDEX `idx_notificaciones_leida` (`leida`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- DATOS DE PRUEBA (SEED DATA)
-- Contraseñas almacenadas con Hash BCrypt seguro ($2a$12$...)
-- Contraseña plana de prueba para todos los usuarios: 'Asilo360#2025'
-- ============================================================

-- 1. Usuarios de prueba (Administrador, Personal, Familiar y Multi-Rol)
INSERT INTO `usuarios` (`id_usuario`, `nombre`, `apellido_paterno`, `apellido_materno`, `correo`, `password`, `id_rol`, `telefono`, `estado`) VALUES
(1, 'Carlos', 'Gómez', 'Hernández', 'admin@asilo360.com', '$2a$12$e83W97o3k3mR8g/a3jE83uK3g9kFp6h6eJ1dK8s7r4w2x5z7y9B0C', 1, '5551234567', TRUE),
(2, 'Laura', 'Martínez', 'López', 'personal@asilo360.com', '$2a$12$e83W97o3k3mR8g/a3jE83uK3g9kFp6h6eJ1dK8s7r4w2x5z7y9B0C', 2, '5559876543', TRUE),
(3, 'María', 'Rodríguez', 'Sánchez', 'familiar@asilo360.com', '$2a$12$e83W97o3k3mR8g/a3jE83uK3g9kFp6h6eJ1dK8s7r4w2x5z7y9B0C', 3, '5555551234', TRUE),
(999, 'Admin2', 'Multi-Rol', 'Sistema', 'admin2@asilo360.com', '$2a$12$e83W97o3k3mR8g/a3jE83uK3g9kFp6h6eJ1dK8s7r4w2x5z7y9B0C', 1, '5555553600', TRUE)
ON DUPLICATE KEY UPDATE `correo` = VALUES(`correo`);

-- 2. Personal vinculado (Personal regular e id_usuario 999)
INSERT INTO `personal` (`id_personal`, `id_usuario`, `puesto`, `fecha_ingreso`, `estado`) VALUES
(1, 2, 'Enfermera General', '2023-01-15', TRUE),
(999, 999, 'Administrador General / Personal', '2023-01-01', TRUE)
ON DUPLICATE KEY UPDATE `puesto` = VALUES(`puesto`);

-- 3. Familiar vinculado (Familiar regular e id_usuario 999)
INSERT INTO `familiares` (`id_familiar`, `id_usuario`, `parentesco`, `telefono`) VALUES
(1, 3, 'Hija', '5555551234'),
(999, 999, 'Administrador / Tutor', '5555553600')
ON DUPLICATE KEY UPDATE `parentesco` = VALUES(`parentesco`);

-- 4. Residente de prueba
INSERT INTO `residentes` (`id_residente`, `nombre`, `apellido_paterno`, `apellido_materno`, `fecha_nacimiento`, `sexo`, `habitacion`, `fecha_ingreso`, `estado`, `observaciones`) VALUES
(1, 'Roberto', 'Rodríguez', 'Pérez', '1945-06-20', 'Masculino', '102-A', '2023-03-10', 'Activo', 'Padece hipertensión controlada. Dieta baja en sodio.')
ON DUPLICATE KEY UPDATE `habitacion` = VALUES(`habitacion`);

-- 5. Relación Residente - Familiar
INSERT INTO `residente_familiar` (`id_residente`, `id_familiar`, `parentesco`) VALUES
(1, 1, 'Hija'),
(1, 999, 'Administrador / Tutor')
ON DUPLICATE KEY UPDATE `parentesco` = VALUES(`parentesco`);

-- 6. Consulta médica de prueba
INSERT INTO `consultas` (`id_residente`, `id_personal`, `fecha`, `hora`, `motivo`, `observaciones`, `estado`) VALUES
(1, 1, CURDATE(), '10:00:00', 'Revision de presion arterial', 'Presion en niveles normales (120/80).', 'Completada');

-- 7. Visita programada
INSERT INTO `visitas` (`id_residente`, `id_familiar`, `fecha`, `hora`, `numero_visitantes`, `estado`) VALUES
(1, 1, CURDATE(), '16:00:00', 2, 'Programada');

-- 8. Pago registrado
INSERT INTO `pagos` (`id_residente`, `monto`, `fecha_pago`, `concepto`, `metodo_pago`, `estado`) VALUES
(1, 12500.00, CURDATE(), 'Mensualidad Residencia - Mes Actual', 'Transferencia', 'Pagado');

-- 9. Actividad realizada
INSERT INTO `actividades` (`id_residente`, `id_personal`, `fecha`, `hora`, `tipo`, `descripcion`) VALUES
(1, 1, CURDATE(), '09:00:00', 'Terapia Física', 'Caminata ligera asistida en el jardín principal.');

-- 10. Notificación de prueba
INSERT INTO `notificaciones` (`id_usuario`, `titulo`, `mensaje`, `tipo`, `leida`) VALUES
(3, 'Visita Programada', 'Tu solicitud de visita para hoy a las 16:00 hrs ha sido confirmada.', 'Visita', FALSE);
