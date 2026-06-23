# Guía del Proyecto — QMS Clínica (Módulo 4)

Documento único: cómo levantar el proyecto, usarlo y entenderlo.

---

## Índice

1. [Cómo levantar el proyecto](#1-cómo-levantar-el-proyecto)
2. [Cómo usar el sistema](#2-cómo-usar-el-sistema)
3. [Qué hace el proyecto](#3-qué-hace-el-proyecto)
4. [Patrones y requisitos del módulo](#4-patrones-y-requisitos-del-módulo)
5. [Problemas frecuentes](#5-problemas-frecuentes)

---

## 1. Cómo levantar el proyecto

### Requisitos

- **JDK 8+** instalado (`java -version` y `javac -version` en PowerShell).
- **XAMPP** con **MySQL** activo (botón Start en verde).
- Carpeta del proyecto: `C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4`

### Paso 1 — Encender MySQL (XAMPP)

1. Abrí **XAMPP Control Panel**.
2. Clic en **Start** junto a **MySQL**.
3. Debe quedar en verde.

### Paso 2 — Crear la base de datos (solo la primera vez)

**Opción A — phpMyAdmin (recomendada):**

1. Abrí `http://localhost/phpmyadmin`
2. Pestaña **Importar**
3. Elegí el archivo `db/clinica_qms_mod4.sql`
4. Clic en **Continuar**

**Opción B — Consola:**

```powershell
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4
mysql -u root < db\clinica_qms_mod4.sql
```

> Si la base ya existe de una ejecución anterior, podés saltear este paso.

### Paso 3 — Descargar el driver JDBC (solo si falta)

```powershell
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4
New-Item -ItemType Directory -Force -Path lib | Out-Null
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar" -OutFile "lib\mysql-connector-j.jar"
```

### Paso 4 — Compilar

```powershell
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4
New-Item -ItemType Directory -Force -Path out | Out-Null
javac --release 8 -cp "lib\mysql-connector-j.jar" -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })
```

Si no hay errores, se crea la carpeta `out/` con las clases compiladas.

### Paso 5 — Ejecutar

```powershell
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4
java -cp "out;lib\mysql-connector-j.jar" com.clinica.qms.App
```

**Conexión OK** — deberías ver:

```text
[OK] Conexion a MySQL establecida. Base: clinica_qms_mod4.
╔══════════════════════════════════════════╗
║   CLINICA MEDICA - SISTEMA QMS - MOD4   ║
╚══════════════════════════════════════════╝
```

**Modo demo** — si ves `[AVISO] ... modo demo`, MySQL no está conectado. Revisá XAMPP, la base importada y el archivo `lib/mysql-connector-j.jar`.

### Comandos rápidos (copiar y pegar)

```powershell
# Ir al proyecto
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4

# Compilar
javac --release 8 -cp "lib\mysql-connector-j.jar" -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })

# Ejecutar
java -cp "out;lib\mysql-connector-j.jar" com.clinica.qms.App

# Pruebas (no requieren MySQL)
java -cp "out;lib\mysql-connector-j.jar" com.clinica.qms.tests.QmsUnitTests
```

### Configuración MySQL

Archivo: `src/main/java/com/clinica/qms/db/ConexionDB.java`

| Parámetro | Valor por defecto (XAMPP) |
|-----------|---------------------------|
| URL       | `jdbc:mysql://localhost:3306/clinica_qms_mod4` |
| Usuario   | `root` |
| Password  | *(vacío)* |

---

## 2. Cómo usar el sistema

### Menú principal

| Opción | Función |
|--------|---------|
| 1 | Autogestión — el paciente pide turno |
| 2 | Login operador |
| 3 | Sala de espera (pantalla pública) |
| 4 | Ver log de auditoría |
| 0 | Salir |

### Operadores predefinidos

| Usuario  | Área           |
|----------|----------------|
| `lperez` | Administración |
| `tacosta`| Laboratorio    |
| `nruiz`  | Internación    |

No tienen contraseña: solo ingresás el usuario y elegís el box.

### Flujo típico de prueba

```
1. Autogestión
   DNI: 30123456 | Nombre: Juan | Apellido: García | Área: 1 (Administración)
   → Turno generado: A-001

2. Login operador
   Usuario: lperez → Box: 1

3. Menú operador
   2 → Llamar siguiente turno
   3 → Derivar a Laboratorio
   4 → Cerrar atención
   5 → Ver métricas
   0 → Cerrar sesión

4. Menú principal
   3 → Ver sala de espera
   4 → Ver log (también en logs/qms_audit.log)
```

### Verificar que MySQL guardó los datos

1. Abrí `http://localhost/phpmyadmin`
2. Base `clinica_qms_mod4` → tabla `turno` → **Examinar**
3. Deberías ver el turno creado con estado y timestamps

---

## 3. Qué hace el proyecto

**QMS Clínica** digitaliza la gestión de turnos en una clínica médica.

| Problema (papel) | Solución (sistema) |
|------------------|-------------------|
| Sin trazabilidad | Timestamps de espera y atención |
| Colas desordenadas | Cola por área con prioridad |
| No se puede derivar | Derivación conservando el mismo código |
| Sin métricas | Reporte de promedios y totales |

**Tecnologías:** Java 8, MySQL (XAMPP), JDBC, archivos de log, patrones MVC + DAO + Singleton + Observer.

**Estructura del código:**

```
src/main/java/com/clinica/qms/
  App.java           → arranque
  db/                → ConexionDB (Singleton)
  dao/               → acceso a datos (MySQL y memoria)
  model/             → Paciente, Turno, Area, Box...
  service/           → GestorTurnos, MetricasServicio
  view/              → ConsolaQmsUI
  observer/          → pantalla y log
  util/              → ArchivoLog, ConsolaUtil, ordenamiento
  tests/             → pruebas CP-01, CP-02, CP-03
db/clinica_qms_mod4.sql
logs/qms_audit.log   → se genera al ejecutar
```

---

## 4. Patrones y requisitos del módulo

### Patrones de diseño

| Patrón | Clase(s) | Por qué |
|--------|----------|---------|
| **MVC** | `ConsolaQmsUI` / `GestorTurnos` / modelos | Separar pantalla, lógica y datos |
| **DAO** | `TurnoDaoMySqlImpl`, `RepositorioFactory` | Cambiar MySQL ↔ memoria sin tocar el servicio |
| **Singleton** | `ConexionDB` | Una sola conexión JDBC reutilizable |
| **Observer** | `ConsolaSalaEsperaObserver`, `ArchivoLogObserver` | Notificar pantalla y log al llamar un turno |

### Requisitos del módulo — dónde están

| Requisito | Implementación |
|-----------|----------------|
| ArrayList | Colas, observers, resultados DAO, log |
| Arrays | Menús (`String[]` en `ConsolaQmsUI`), reporte (`String[]` en `MetricasServicio`) |
| MySQL | DAOs MySQL + script `db/clinica_qms_mod4.sql` + XAMPP |
| Archivos | `ArchivoLog` → `logs/qms_audit.log` (escritura y lectura) |
| Excepciones | `BusinessException`, `DataAccessException`, try-with-resources |
| POO | Herencia (`Persona`), interfaces (`IDao`, `PantallaObserver`), clases abstractas |

---

## 5. Problemas frecuentes

| Síntoma | Solución |
|---------|----------|
| `modo demo (datos en memoria)` | Encender MySQL en XAMPP e importar el SQL |
| `Driver MySQL no encontrado` | Descargar `lib/mysql-connector-j.jar` (Paso 3) |
| Error al compilar | Verificar JDK: `javac -version` |
| `Usuario no encontrado` | MySQL no cargó áreas → revisar conexión y SQL |
| Turno no aparece en phpMyAdmin | Corriste en modo demo → reiniciar con MySQL activo |
| Cola vacía al llamar | El turno está en otra área → verificar área del operador |

---

*Proyecto Integrador — Módulo 4 — QMS Clínica*
