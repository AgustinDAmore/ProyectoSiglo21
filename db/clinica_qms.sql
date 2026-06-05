-- Crea la base de datos principal del sistema de turnos
CREATE DATABASE IF NOT EXISTS clinica_qms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinica_qms;

-- Tabla de pacientes registrados
CREATE TABLE IF NOT EXISTS paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(15) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Areas donde se atienden los turnos
CREATE TABLE IF NOT EXISTS area (
    id_area INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    letra_identificadora CHAR(1) NOT NULL UNIQUE,
    activa TINYINT(1) NOT NULL DEFAULT 1
);

-- Boxes de atencion por cada area
CREATE TABLE IF NOT EXISTS box_atencion (
    id_box INT AUTO_INCREMENT PRIMARY KEY,
    id_area INT NOT NULL,
    numero_identificador INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    UNIQUE KEY uk_area_box (id_area, numero_identificador),
    CONSTRAINT fk_box_area FOREIGN KEY (id_area) REFERENCES area(id_area)
);

-- Operadores del sistema (recepcion, laboratorio, etc.)
CREATE TABLE IF NOT EXISTS operador (
    id_operador INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    id_area INT NOT NULL,
    id_box INT NULL,
    CONSTRAINT fk_operador_area FOREIGN KEY (id_area) REFERENCES area(id_area),
    CONSTRAINT fk_operador_box FOREIGN KEY (id_box) REFERENCES box_atencion(id_box)
);

-- Turnos generados para los pacientes
CREATE TABLE IF NOT EXISTS turno (
    id_turno INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_ESPERA',
    prioridad INT NOT NULL DEFAULT 0,
    timestamp_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    timestamp_llamado DATETIME NULL,
    timestamp_inicio_atencion DATETIME NULL,
    timestamp_fin_atencion DATETIME NULL,
    area_origen VARCHAR(50) NULL,
    id_paciente INT NOT NULL,
    id_area INT NOT NULL,
    id_box INT NULL,
    CONSTRAINT fk_turno_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    CONSTRAINT fk_turno_area FOREIGN KEY (id_area) REFERENCES area(id_area),
    CONSTRAINT fk_turno_box FOREIGN KEY (id_box) REFERENCES box_atencion(id_box)
);

-- Historial de cambios/eventos de cada turno
CREATE TABLE IF NOT EXISTS historial_evento_turno (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    id_turno INT NOT NULL,
    evento VARCHAR(50) NOT NULL,
    detalle VARCHAR(255),
    fecha_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento_turno FOREIGN KEY (id_turno) REFERENCES turno(id_turno)
);

-- Indice para agilizar consultas de cola por area/estado/prioridad
CREATE INDEX idx_turno_area_estado_prioridad
    ON turno(id_area, estado, prioridad, timestamp_creacion);

-- Indice para busqueda rapida por codigo de turno
CREATE INDEX idx_turno_codigo
    ON turno(codigo);

-- Consulta base para metricas por area.
-- Se usa FINALIZADO como estado canonico (y ATENDIDO por compatibilidad historica).
CREATE OR REPLACE VIEW vw_metricas_espera_diaria AS
SELECT
    a.nombre AS area,
    AVG(TIMESTAMPDIFF(MINUTE, t.timestamp_creacion, t.timestamp_llamado)) AS espera_promedio_min
FROM turno t
JOIN area a ON a.id_area = t.id_area
WHERE t.estado IN ('FINALIZADO', 'ATENDIDO')
GROUP BY a.nombre;

-- Datos base de areas
INSERT INTO area (nombre, letra_identificadora) VALUES
('Administracion', 'A'),
('Laboratorio', 'L'),
('Internacion', 'I')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Carga inicial de boxes por area
INSERT INTO box_atencion (id_area, numero_identificador, estado)
SELECT a.id_area, 1, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Administracion'
UNION ALL
SELECT a.id_area, 2, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Administracion'
UNION ALL
SELECT a.id_area, 3, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Administracion'
UNION ALL
SELECT a.id_area, 4, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Administracion'
UNION ALL
SELECT a.id_area, 1, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Laboratorio'
UNION ALL
SELECT a.id_area, 2, 'DISPONIBLE' FROM area a WHERE a.nombre = 'Laboratorio'
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

-- Operadores y passwords se gestionan desde la tabla operador.
-- En la primera ejecucion, la app crea usuarios semilla con hash BCrypt.
