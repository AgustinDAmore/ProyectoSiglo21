# QMS Clínica ? Módulo 4

Sistema de gestión de turnos para clínica médica. Java + MySQL (XAMPP) + JDBC.

## Documentación

| Documento | Contenido |
|-----------|-----------|
| [`docs/GUIA.md`](docs/GUIA.md) | Cómo levantar, usar y resolver problemas |
| [`docs/Modulo4_Explicacion_Desarrollo_Java.md`](docs/Modulo4_Explicacion_Desarrollo_Java.md) | Explicación completa: estructura, lógica de negocio, excepciones, menús, POO |
| [`docs/Modulo4_Presentacion_Java.md`](docs/Modulo4_Presentacion_Java.md) | Guía de presentación oral / diapositivas |
| [`docs/capturas/`](docs/capturas/) | Capturas de pantalla del menú y flujos |

## Levantar en 3 pasos

```powershell
cd C:\Users\Agustin\Desktop\ProyectoSiglo21\proyectoMod4

# 1. XAMPP ? MySQL Start (verde)
# 2. Compilar (primera vez: importar db/clinica_qms_mod4.sql y descargar lib/mysql-connector-j.jar)
javac --release 8 -cp "lib\mysql-connector-j.jar" -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })

# 3. Ejecutar
java -cp "out;lib\mysql-connector-j.jar" com.clinica.qms.App
```

Debe aparecer: `[OK] Conexion a MySQL establecida. Base: clinica_qms_mod4.`

## Operadores de prueba

| Usuario  | Área           |
|----------|----------------|
| lperez   | Administración |
| tacosta  | Laboratorio    |
| nruiz    | Internación    |
