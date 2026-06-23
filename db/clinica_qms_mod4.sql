-- ============================================================
-- QMS Clinica - Modulo 4 - Base de datos MySQL
-- Importar en phpMyAdmin (XAMPP) o consola MySQL
-- ============================================================
CREATE DATABASE IF NOT EXISTS clinica_qms_mod4 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinica_qms_mod4;

-- -------------------------------------------------------
-- Tabla: paciente
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS paciente (
    id_paciente  INT          AUTO_INCREMENT PRIMARY KEY,
    dni          VARCHAR(15)  NOT NULL UNIQUE,
    nombre       VARCHAR(100) NOT NULL,
    apellido     VARCHAR(100) NOT NULL,
    fecha_alta   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Tabla: area
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS area (
    id_area               INT         AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(50) NOT NULL UNIQUE,
    letra_identificadora  CHAR(1)     NOT NULL UNIQUE,
    activa                TINYINT(1)  NOT NULL DEFAULT 1
);

-- -------------------------------------------------------
-- Tabla: box_atencion
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS box_atencion (
    id_box               INT         AUTO_INCREMENT PRIMARY KEY,
    id_area              INT         NOT NULL,
    numero_identificador INT         NOT NULL,
    estado               VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    UNIQUE KEY uk_area_box (id_area, numero_identificador),
    CONSTRAINT fk_box_area FOREIGN KEY (id_area) REFERENCES area(id_area)
);

-- -------------------------------------------------------
-- Tabla: consecutivo_turno
-- Garantiza codigos unicos sin condiciones de carrera
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS consecutivo_turno (
    id_area       INT PRIMARY KEY,
    ultimo_numero INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_consecutivo_area FOREIGN KEY (id_area) REFERENCES area(id_area)
);

-- -------------------------------------------------------
-- Tabla: turno
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS turno (
    id_turno                INT         AUTO_INCREMENT PRIMARY KEY,
    codigo                  VARCHAR(10) NOT NULL UNIQUE,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'EN_ESPERA',
    prioridad               INT         NOT NULL DEFAULT 0,
    timestamp_creacion      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    timestamp_llamado       DATETIME    NULL,
    timestamp_inicio_aten   DATETIME    NULL,
    timestamp_fin_aten      DATETIME    NULL,
    area_origen             VARCHAR(50) NULL,
    id_paciente             INT         NOT NULL,
    id_area                 INT         NOT NULL,
    id_box                  INT         NULL,
    CONSTRAINT fk_turno_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    CONSTRAINT fk_turno_area     FOREIGN KEY (id_area)     REFERENCES area(id_area),
    CONSTRAINT fk_turno_box      FOREIGN KEY (id_box)      REFERENCES box_atencion(id_box)
);

CREATE INDEX idx_turno_area_estado ON turno(id_area, estado, prioridad, timestamp_creacion);
CREATE INDEX idx_turno_codigo      ON turno(codigo);

-- -------------------------------------------------------
-- Tabla: historial_evento_turno  (auditoria)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_evento_turno (
    id_evento   INT          AUTO_INCREMENT PRIMARY KEY,
    id_turno    INT          NOT NULL,
    evento      VARCHAR(50)  NOT NULL,
    detalle     VARCHAR(255) NULL,
    fecha_evento DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento_turno FOREIGN KEY (id_turno) REFERENCES turno(id_turno)
);

-- -------------------------------------------------------
-- Datos semilla: Areas
-- -------------------------------------------------------
INSERT INTO area (nombre, letra_identificadora) VALUES
    ('Administracion', 'A'),
    ('Laboratorio',    'L'),
    ('Internacion',    'I')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- -------------------------------------------------------
-- Datos semilla: Boxes
-- -------------------------------------------------------
INSERT INTO box_atencion (id_area, numero_identificador)
SELECT a.id_area, n.num
FROM area a
JOIN (SELECT 1 AS num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) n
WHERE a.nombre = 'Administracion'
ON DUPLICATE KEY UPDATE estado = estado;

INSERT INTO box_atencion (id_area, numero_identificador)
SELECT a.id_area, n.num
FROM area a
JOIN (SELECT 1 AS num UNION ALL SELECT 2) n
WHERE a.nombre = 'Laboratorio'
ON DUPLICATE KEY UPDATE estado = estado;

INSERT INTO box_atencion (id_area, numero_identificador)
SELECT a.id_area, 1 FROM area a WHERE a.nombre = 'Internacion'
ON DUPLICATE KEY UPDATE estado = estado;

-- -------------------------------------------------------
-- Datos semilla: Consecutivos por area
-- -------------------------------------------------------
INSERT INTO consecutivo_turno (id_area, ultimo_numero)
SELECT id_area, 0 FROM area
ON DUPLICATE KEY UPDATE ultimo_numero = ultimo_numero;
