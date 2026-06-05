package com.clinica.qms.tests;

import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.repository.InMemoryTurnoRepository;
import com.clinica.qms.service.GestorTurnos;

public class QmsUnitTests {

    public static void main(String[] args) {
        // Runner simple sin framework externo.
        ejecutar("CP-01 Generacion sin duplicados", QmsUnitTests::testCp01GeneracionSinDuplicados);
        ejecutar("CP-02 Prioridad en derivacion", QmsUnitTests::testCp02PrioridadDerivacion);
        ejecutar("CP-03 Tiempo de actualizacion", QmsUnitTests::testCp03TiempoRespuestaLlamado);
        System.out.println("\nPruebas finalizadas.");
    }

    private static void testCp01GeneracionSinDuplicados() {
        // Valida codigos unicos y correlativos para la misma area.
        Area administracion = new Area(1, "Administracion", 'A');
        GestorTurnos gestor = new GestorTurnos(new InMemoryTurnoRepository());

        Turno t1 = gestor.solicitarTurno(new Paciente(1, "Paciente Uno", "30111222"), administracion);
        Turno t2 = gestor.solicitarTurno(new Paciente(2, "Paciente Dos", "30999333"), administracion);

        assertTrue(!t1.getCodigo().equals(t2.getCodigo()), "Se genero codigo duplicado");
        assertEquals("A-001", t1.getCodigo(), "Primer codigo incorrecto");
        assertEquals("A-002", t2.getCodigo(), "Segundo codigo incorrecto");
    }

    private static void testCp02PrioridadDerivacion() {
        // Valida que una derivacion pase al frente de la cola.
        Area administracion = new Area(1, "Administracion", 'A');
        Area laboratorio = new Area(2, "Laboratorio", 'L');
        laboratorio.agregarBox(new Box(10, 1));

        GestorTurnos gestor = new GestorTurnos(new InMemoryTurnoRepository());

        Turno normalLaboratorio = gestor.solicitarTurno(new Paciente(1, "Paciente Lab", "31111222"), laboratorio);
        Turno turnoAdmin = gestor.solicitarTurno(new Paciente(2, "Paciente Admin", "32222333"), administracion);

        gestor.derivarTurno(turnoAdmin.getCodigo(), laboratorio);

        Turno llamado = gestor.llamarSiguiente(laboratorio, laboratorio.getBoxes().get(0)).orElse(null);

        assertTrue(llamado != null, "No se obtuvo turno para llamar");
        assertEquals(turnoAdmin.getCodigo(), llamado.getCodigo(), "La derivacion no tuvo prioridad");
        assertEquals(EstadoTurno.EN_ATENCION, llamado.getEstado(), "El turno llamado no quedo en atencion");
        assertTrue(!llamado.getCodigo().equals(normalLaboratorio.getCodigo()), "Se llamo primero al turno normal");
    }

    private static void testCp03TiempoRespuestaLlamado() {
        // Mide que el llamado basico sea rapido.
        Area laboratorio = new Area(2, "Laboratorio", 'L');
        Box box = new Box(20, 1);
        laboratorio.agregarBox(box);

        GestorTurnos gestor = new GestorTurnos(new InMemoryTurnoRepository());
        gestor.solicitarTurno(new Paciente(1, "Paciente Rapido", "33333444"), laboratorio);

        long inicio = System.nanoTime();
        Turno llamado = gestor.llamarSiguiente(laboratorio, box).orElse(null);
        long fin = System.nanoTime();

        long milisegundos = (fin - inicio) / 1_000_000;

        assertTrue(llamado != null, "No se llamo ningun turno");
        assertTrue(milisegundos < 2000, "El llamado supero 2 segundos: " + milisegundos + "ms");
    }

    private static void ejecutar(String nombre, Runnable test) {
        try {
            test.run();
            System.out.println("[OK] " + nombre);
        } catch (AssertionError ex) {
            System.out.println("[FAIL] " + nombre + " -> " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("[ERROR] " + nombre + " -> " + ex.getMessage());
        }
    }

    private static void assertTrue(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void assertEquals(Object esperado, Object actual, String mensaje) {
        if (esperado == null && actual == null) {
            return;
        }
        if (esperado != null && esperado.equals(actual)) {
            return;
        }
        throw new AssertionError(mensaje + ". Esperado: " + esperado + ", actual: " + actual);
    }
}
