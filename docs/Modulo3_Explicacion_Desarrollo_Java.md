# Explicacion del Desarrollo en Java - Modulo 3

## 1. Objetivo del modulo
En este modulo se implementa el prototipo del sistema QMS aplicando Programacion Orientada a Objetos con Java, respetando MVC y conectando con MySQL mediante JDBC.

## 2. Aplicacion de POO

### Encapsulamiento
Las clases del dominio (`Paciente`, `Turno`, `Area`, `Box`, `Operador`) exponen sus atributos mediante metodos `get` y limitan cambios mediante metodos de negocio (`marcarLlamado`, `finalizarAtencion`, `derivar`).

### Herencia
Se define una clase abstracta `Persona` y se reutiliza en `Paciente` y `Operador`.

### Polimorfismo
La persistencia se abstrae con la interfaz `TurnoRepository`, permitiendo reemplazar implementaciones (`InMemoryTurnoRepository` y `MySqlTurnoRepository`) sin modificar la logica del servicio.

### Abstraccion
`GestorTurnos` encapsula reglas de negocio: generacion de codigo, llamado por area/box, derivaciones con prioridad y calculo de metricas.

## 3. Control de flujo y excepciones
- Estructuras condicionales para validar areas/boxes y estado de turnos.
- Estructuras repetitivas para simulacion de operaciones y reportes.
- `BusinessException` para manejar errores de negocio (sin romper el flujo principal).

## 4. Estructuras de datos y algoritmos
- `Map<Character, Integer>` para consecutivos de turnos por area.
- `LinkedList<Turno>` para cola de espera por area.
- Derivaciones con prioridad usando insercion al inicio de la cola.
- Ordenamiento y busqueda en `OrdenamientoBusqueda`:
  - Ordenamiento por tiempo de espera.
  - Busqueda binaria por codigo de turno.

## 5. Modelo MVC aplicado
- Modelo: clases en paquete `model`.
- Controlador/servicio: `GestorTurnos`.
- Vista: interfaz de consola interactiva en `ConsolaQmsUI` con menu principal y menu de operador.

### 5.1. Interfaz implementada
La interfaz de consola cubre el flujo solicitado:
- Login de operador por usuario y password (validado con hash BCrypt).
- Seleccion de box en el area asignada.
- Llamado del siguiente paciente.
- Derivacion manteniendo el mismo codigo de turno y prioridad alta.
- Cierre de atencion por box.
- Visualizacion de pantalla de espera y metricas globales.
- ABM basico de areas y boxes desde consola (alta, modificacion, baja y cambio de estado de box).

## 6. Base de datos MySQL (XAMPP)
El archivo `db/clinica_qms.sql` crea:
- Esquema `clinica_qms`.
- Tablas normalizadas: `paciente`, `area`, `box_atencion`, `operador`, `turno`, `historial_evento_turno`.
- Indices para optimizar busquedas por area/estado/prioridad.
- Datos semilla de areas y boxes.
- Operadores y contraseñas almacenados en la tabla `operador` (password hash).

## 7. Resultado
El proyecto cumple con el enfoque del Modulo 3:
- Desarrollo orientado a objetos.
- Uso de herencia, abstraccion, polimorfismo y encapsulamiento.
- Control de flujo y excepciones.
- Uso de colecciones y algoritmos.
- Preparacion para persistencia real con JDBC y MySQL.

## 8. Pruebas unitarias CP-01, CP-02 y CP-03
Se agrego un runner de pruebas automatizadas en `QmsUnitTests`:
- **CP-01:** valida codigos consecutivos sin duplicados (A-001, A-002).
- **CP-02:** valida prioridad de derivacion en la cola de destino.
- **CP-03:** valida tiempo de actualizacion del llamado en menos de 2 segundos.
