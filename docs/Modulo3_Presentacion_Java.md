# Presentacion - Desarrollo del Sistema QMS en Java

## Diapositiva 1 - Problema
- Gestion manual de turnos por papel.
- Sin trazabilidad de espera y atencion.
- Derivaciones lentas (el paciente vuelve a sacar numero).

## Diapositiva 2 - Objetivo
Desarrollar un prototipo Java que centralice:
- Emision de turnos.
- Llamado por box.
- Derivaciones con prioridad.
- Metricas operativas en tiempo real.

## Diapositiva 3 - Arquitectura
- Patron MVC.
- Java 8+ + Maven.
- JDBC + MySQL (XAMPP).
- Red LAN para baja latencia.

## Diapositiva 4 - Modelo de dominio
Entidades principales:
- Paciente
- Turno
- Area
- Box
- Operador

Relaciones:
- Paciente solicita turnos.
- Turno pertenece a area y se atiende en box.
- Operador atiende en un box.

## Diapositiva 5 - POO aplicada
- Encapsulamiento: atributos privados + metodos de negocio.
- Herencia: `Persona` -> `Paciente`/`Operador`.
- Polimorfismo: `TurnoRepository` con implementaciones multiple.
- Abstraccion: `GestorTurnos` concentra reglas del negocio.

## Diapositiva 6 - Flujo principal
1. Paciente ingresa DNI y area.
2. Sistema genera turno (ej: A-034).
3. Operador llama siguiente.
4. Pantalla publica muestra turno + box.
5. Si hay derivacion, conserva codigo y pasa primero en nueva area.

## Diapositiva 6.1 - Interfaz implementada
- Menu principal de consola para autogestion, login, pantalla de sala y ABM de areas/boxes.
- Menu de operador con seleccion de box, llamado, derivacion y cierre de atencion.
- Visualizacion de metricas de espera y atencion.

## Diapositiva 6.2 - Seguridad y acceso
- Login por usuario y password.
- Password almacenada como hash (SHA-256 + Base64) en tabla operador.
- Operadores cargados desde MySQL.

## Diapositiva 7 - Estructuras y algoritmos
- `LinkedList` para cola de espera por area.
- `Map` para consecutivos por letra de area.
- Algoritmo de ordenamiento por tiempo de espera.
- Busqueda binaria por codigo de turno.

## Diapositiva 8 - Base de datos
- Script SQL: `db/clinica_qms.sql`.
- Tablas: paciente, area, box_atencion, operador, turno, historial_evento_turno.
- Indices para ruteo rapido de cola.

## Diapositiva 9 - Pruebas sugeridas
- CP-01: generacion sin duplicados.
- CP-02: prioridad de derivacion.
- CP-03: actualizacion de sala en < 2 segundos.

## Diapositiva 9.1 - Pruebas ejecutadas
- Se implementaron y ejecutaron pruebas automatizadas para CP-01, CP-02 y CP-03.
- Resultado: todas las pruebas en estado OK.

## Diapositiva 10 - Cierre
El prototipo cumple los requerimientos del Modulo 3 y deja base lista para evolucionar a interfaz grafica o web sin cambiar reglas centrales de negocio.
