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
- `docs/Como_Ejecutar_QMS.md`: Guia de ejecucion actualizada paso a paso.

## Como ejecutar
Ver guia completa en `docs/Como_Ejecutar_QMS.md`.

Resumen rapido (modo MySQL):
1. Importar BD con `db/clinica_qms.sql`.
2. Configurar `QMS_REPO`, `QMS_DB_URL`, `QMS_DB_USER`, `QMS_DB_PASSWORD`.
3. Ejecutar con Maven (`mvn clean compile` + `mvn exec:java`) o sin Maven (javac + classpath con `mysql-connector-j`).

## Ejecucion sin Maven (como se valido en este entorno)
Seguir `docs/Como_Ejecutar_QMS.md` para incluir el driver MySQL en el classpath.

## Login de operadores (modo MySQL)
- Los operadores se leen desde la tabla `operador`.
- Si la tabla esta vacia, la app crea usuarios semilla con password hasheada (SHA-256 + Base64):
   - `lperez / admin123`
   - `tacosta / lab123`
   - `nruiz / inter123`
