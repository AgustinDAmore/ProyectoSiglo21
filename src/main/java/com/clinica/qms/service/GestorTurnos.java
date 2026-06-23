package com.clinica.qms.service;

import com.clinica.qms.dao.PacienteDao;
import com.clinica.qms.dao.TurnoDao;
import com.clinica.qms.exception.BusinessException;
import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.observer.PantallaObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador principal del sistema.
 * Orquesta el flujo de turnos y notifica a los observers registrados.
 */
public class GestorTurnos {

    private final TurnoDao    turnoDao;
    private final PacienteDao pacienteDao;
    private final ArrayList<Area> areas;

    private final ArrayList<PantallaObserver> observers;

    /** Cola FIFO con prioridad por area: prioridad DESC, creacion ASC. */
    private final Map<Integer, LinkedList<Turno>> colaPorArea;

    /** Box -> turno activo en ese box. */
    private final Map<Integer, Turno> turnoActivoPorBox;

    public GestorTurnos(TurnoDao turnoDao, PacienteDao pacienteDao, ArrayList<Area> areas) {
        this.turnoDao         = turnoDao;
        this.pacienteDao      = pacienteDao;
        this.areas            = areas;
        this.observers        = new ArrayList<PantallaObserver>();
        this.colaPorArea      = new HashMap<Integer, LinkedList<Turno>>();
        this.turnoActivoPorBox = new HashMap<Integer, Turno>();

        // Inicializar colas vacias para cada area
        for (Area a : areas) {
            colaPorArea.put(a.getId(), new LinkedList<Turno>());
        }
    }

    // ----------------------------------------------------------------
    // Observers registrados
    // ----------------------------------------------------------------
    public void agregarObserver(PantallaObserver obs) {
        observers.add(obs);
    }

    private void notificarLlamado(Turno turno) {
        for (PantallaObserver obs : observers) obs.onTurnoLlamado(turno);
    }

    private void notificarCerrado(Turno turno) {
        for (PantallaObserver obs : observers) obs.onTurnoCerrado(turno);
    }

    private void notificarDerivado(Turno turno, String areaDestino) {
        for (PantallaObserver obs : observers) obs.onTurnoDerivado(turno, areaDestino);
    }

    // ----------------------------------------------------------------
    // API publica
    // ----------------------------------------------------------------

    /**
     * Registra o recupera al paciente por DNI y genera un turno nuevo.
     */
    public Turno solicitarTurno(String dni, String nombre, String apellido, Area area)
            throws DataAccessException {
        // Buscar o crear paciente
        Optional<Paciente> opt = pacienteDao.buscarPorDni(dni);
        Paciente paciente;
        if (opt.isPresent()) {
            paciente = opt.get();
        } else {
            paciente = pacienteDao.guardar(new Paciente(0, nombre, apellido, dni));
        }

        // Generar codigo atomico
        String codigo = turnoDao.generarSiguienteCodigo(area);

        // Crear y persistir turno
        Turno turno = new Turno(codigo, paciente, area);
        turnoDao.guardar(turno);

        // Encolar en memoria
        encolar(turno);
        return turno;
    }

    /** Llama al siguiente turno en la cola del area para el box indicado. */
    public Turno llamarSiguiente(Area area, Box box) throws DataAccessException {
        LinkedList<Turno> cola = obtenerCola(area);
        if (cola.isEmpty()) {
            throw new BusinessException("No hay turnos en espera para " + area.getNombre());
        }
        Turno turno = cola.poll();
        turno.marcarLlamado(box);
        turno.iniciarAtencion();
        turnoActivoPorBox.put(box.getId(), turno);
        turnoDao.actualizar(turno);
        notificarLlamado(turno);
        return turno;
    }

    /** Deriva el turno identificado por codigo hacia un area de destino. */
    public Turno derivarTurno(String codigo, Area areaDestino) throws DataAccessException {
        Turno turno = buscarTurnoActivoPorCodigo(codigo);
        if (turno == null) {
            Optional<Turno> opt = turnoDao.buscarPorCodigo(codigo);
            if (!opt.isPresent()) {
                throw new BusinessException("Turno no encontrado: " + codigo);
            }
            turno = opt.get();
        }
        String areaOrigenNombre = turno.getAreaActual().getNombre();
        turno.derivar(areaDestino, areaOrigenNombre);
        turnoDao.actualizar(turno);

        // Insertar al frente de la cola del area de destino (prioridad alta)
        obtenerCola(areaDestino).addFirst(turno);
        notificarDerivado(turno, areaDestino.getNombre());
        return turno;
    }

    private Turno buscarTurnoActivoPorCodigo(String codigo) {
        for (Turno t : turnoActivoPorBox.values()) {
            if (t.getCodigo().equals(codigo)) {
                return t;
            }
        }
        return null;
    }

    /** Cierra la atencion del turno activo en el box indicado. */
    public Turno cerrarAtencion(Box box) throws DataAccessException {
        Turno turno = turnoActivoPorBox.get(box.getId());
        if (turno == null) {
            throw new BusinessException("No hay atencion activa en " + box);
        }
        if (turno.getEstado() == EstadoTurno.DERIVADO) {
            turno.registrarFinAtencion();
        } else {
            turno.finalizarAtencion();
        }
        turnoActivoPorBox.remove(box.getId());
        turnoDao.actualizar(turno);
        notificarCerrado(turno);
        return turno;
    }

    /** Devuelve el turno activo en un box, o null si esta libre. */
    public Turno obtenerTurnoActivo(Box box) {
        return turnoActivoPorBox.get(box.getId());
    }

    public ArrayList<Turno> listarEsperaPorArea(Area area) throws DataAccessException {
        ArrayList<Turno> lista = new ArrayList<Turno>(obtenerCola(area));
        return lista;
    }

    public ArrayList<Turno> listarHistorico() throws DataAccessException {
        return turnoDao.buscarTodos();
    }

    public ArrayList<Area> obtenerAreas() {
        return new ArrayList<Area>(areas);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private void encolar(Turno turno) {
        LinkedList<Turno> cola = obtenerCola(turno.getAreaActual());
        if (turno.getPrioridad() > 0) {
            cola.addFirst(turno);
        } else {
            cola.addLast(turno);
        }
    }

    private LinkedList<Turno> obtenerCola(Area area) {
        LinkedList<Turno> cola = colaPorArea.get(area.getId());
        if (cola == null) {
            cola = new LinkedList<Turno>();
            colaPorArea.put(area.getId(), cola);
        }
        return cola;
    }
}
