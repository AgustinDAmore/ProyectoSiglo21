package com.clinica.qms.tests;

import com.clinica.qms.dao.TurnoDaoMemoriaImpl;
import com.clinica.qms.dao.PacienteDaoMemoriaImpl;
import com.clinica.qms.dao.AreaDaoMemoriaImpl;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.service.GestorTurnos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Suite de pruebas unitarias para el Modulo 4.
 * Sin dependencias externas (no requiere JUnit ni MySQL).
 * Reutiliza las implementaciones en memoria de todos los DAOs.
 */
public class QmsUnitTests {

    private static int pasadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== SUITE DE PRUEBAS QMS - MODULO 4 ===\n");
        CP01_codigos_sin_duplicados();
        CP02_prioridad_de_derivacion();
        CP03_tiempo_llamado_menor_2_segundos();
        System.out.println("\n=================================");
        System.out.println("RESULTADO: " + pasadas + " PASS  |  " + fallidas + " FAIL");
        System.out.println("=================================");
    }

    // ----------------------------------------------------------------
    // CP-01: Los codigos de turno no se duplican
    // ----------------------------------------------------------------
    private static void CP01_codigos_sin_duplicados() {
        try {
            GestorTurnos gestor = crearGestor();
            ArrayList<Area> areas = gestor.obtenerAreas();
            Area admin = areas.get(0);
            Box  box   = admin.getBoxes().get(0);

            Set<String> codigos = new HashSet<String>();
            for (int i = 0; i < 20; i++) {
                Turno t = gestor.solicitarTurno("DNI" + i, "P" + i, "A", admin);
                codigos.add(t.getCodigo());
            }
            assertTrue("CP-01: Codigos sin duplicados (20 turnos)",
                       codigos.size() == 20);
        } catch (Exception e) {
            fail("CP-01", e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // CP-02: Turno derivado tiene prioridad alta (aparece primero en cola)
    // ----------------------------------------------------------------
    private static void CP02_prioridad_de_derivacion() {
        try {
            GestorTurnos gestor = crearGestor();
            ArrayList<Area> areas = gestor.obtenerAreas();
            Area admin = areas.get(0);
            Area lab   = areas.get(1);
            Box  boxA  = admin.getBoxes().get(0);

            // Generar 3 turnos en admin
            Turno t1 = gestor.solicitarTurno("11111111", "Juan",  "Perez",  admin);
            Turno t2 = gestor.solicitarTurno("22222222", "Maria", "Lopez",  admin);
            Turno t3 = gestor.solicitarTurno("33333333", "Carlos","Gomez",  admin);

            // Llamar t1 y derivarlo a laboratorio
            Turno llamado = gestor.llamarSiguiente(admin, boxA);
            gestor.derivarTurno(llamado.getCodigo(), lab);

            // Generar turno nuevo en lab
            Turno t4 = gestor.solicitarTurno("44444444", "Ana",   "Ruiz",   lab);

            // El primer turno en la cola de lab debe ser el derivado (prioridad alta)
            ArrayList<Turno> colaLab = gestor.listarEsperaPorArea(lab);
            assertTrue("CP-02: Derivado aparece primero en la cola de destino",
                       !colaLab.isEmpty() && colaLab.get(0).getPrioridad() > 0);
        } catch (Exception e) {
            fail("CP-02", e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // CP-03: El tiempo de respuesta de llamar siguiente es < 2 segundos
    // ----------------------------------------------------------------
    private static void CP03_tiempo_llamado_menor_2_segundos() {
        try {
            GestorTurnos gestor = crearGestor();
            ArrayList<Area> areas = gestor.obtenerAreas();
            Area admin = areas.get(0);
            Box  box   = admin.getBoxes().get(0);

            gestor.solicitarTurno("55555555", "Elena", "Torres", admin);

            long inicio = System.currentTimeMillis();
            Turno llamado = gestor.llamarSiguiente(admin, box);
            long ms = System.currentTimeMillis() - inicio;

            assertTrue("CP-03: llamarSiguiente se ejecuta en < 2000 ms (actual: " + ms + " ms)",
                       ms < 2000 && llamado.getEstado() == EstadoTurno.EN_ATENCION);
        } catch (Exception e) {
            fail("CP-03", e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static GestorTurnos crearGestor() throws Exception {
        AreaDaoMemoriaImpl areaDao     = new AreaDaoMemoriaImpl();
        PacienteDaoMemoriaImpl pacDao  = new PacienteDaoMemoriaImpl();
        TurnoDaoMemoriaImpl turnoDao   = new TurnoDaoMemoriaImpl();
        ArrayList<Area> areas = areaDao.buscarTodos();
        return new GestorTurnos(turnoDao, pacDao, areas);
    }

    private static void assertTrue(String nombre, boolean condicion) {
        if (condicion) {
            System.out.println("[PASS] " + nombre);
            pasadas++;
        } else {
            System.out.println("[FAIL] " + nombre);
            fallidas++;
        }
    }

    private static void fail(String nombre, String motivo) {
        System.out.println("[FAIL] " + nombre + " -> " + motivo);
        fallidas++;
    }
}
