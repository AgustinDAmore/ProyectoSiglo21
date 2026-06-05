# QMS Clinica - Modulo 3 (Java)

Este proyecto implementa un prototipo del Sistema Integral de Gestion de Turnos, Derivaciones y Metricas de Atencion para clinica medica.

## Tecnologias
- Java 8+
- Maven
- MySQL (XAMPP)
- JDBC
- Arquitectura MVC

## Estructura
- `src/main/java/com/clinica/qms/model`: Entidades del dominio.
- `src/main/java/com/clinica/qms/service`: Logica de negocio.
- `src/main/java/com/clinica/qms/repository`: Persistencia (en memoria y JDBC).
- `src/main/java/com/clinica/qms/util`: Algoritmos de busqueda/ordenamiento.
- `src/main/java/com/clinica/qms/view`: Interfaz de consola interactiva.
- `src/main/java/com/clinica/qms/tests/QmsUnitTests.java`: Pruebas CP-01, CP-02 y CP-03.
- `db/clinica_qms.sql`: Script de base de datos para XAMPP/MySQL.
- `docs/Modulo3_Explicacion_Desarrollo_Java.md`: Entregable de explicacion.
- `docs/Modulo3_Presentacion_Java.md`: Entregable para presentacion.

## Como ejecutar
1. Importar la BD en XAMPP (phpMyAdmin o consola):
   - Crear/importar con `db/clinica_qms.sql`.
2. Configurar variables de entorno para usar MySQL:
   - `QMS_REPO=mysql`
   - `QMS_DB_URL=jdbc:mysql://localhost:3306/clinica_qms`
   - `QMS_DB_USER=root`
   - `QMS_DB_PASSWORD=`
3. Compilar:
   - `mvn clean compile`
4. Ejecutar interfaz de consola:
   - `mvn exec:java`

## Ejecucion sin Maven (como se valido en este entorno)
1. Compilar:
   - `javac --release 8 -d out (Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName })`
2. Ejecutar interfaz:
   - `java -cp out com.clinica.qms.App`
3. Ejecutar pruebas CP:
   - `java -cp out com.clinica.qms.tests.QmsUnitTests`

## Login de operadores (modo MySQL)
- Los operadores se leen desde la tabla `operador`.
- Si la tabla esta vacia, la app crea usuarios semilla con password hasheada (BCrypt):
   - `lperez / admin123`
   - `tacosta / lab123`
   - `nruiz / inter123`
