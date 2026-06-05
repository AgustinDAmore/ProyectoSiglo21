package com.clinica.qms.service;

import com.clinica.qms.exception.BusinessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Paciente;
import com.clinica.qms.model.Turno;
import com.clinica.qms.repository.TurnoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GestorTurnos {
    private final TurnoRepository turnoRepository;
    private final Map<Character, Integer> consecutivos;
    private final Map<Integer, LinkedList<Turno>> colaPorArea;
    private final Map<Integer, Turno> turnoActivoPorBox;
    private int secuenciaInterna;

    public GestorTurnos(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
        this.consecutivos = new HashMap<>();
        this.colaPorArea = new HashMap<>();
        this.turnoActivoPorBox = new HashMap<>();
        this.secuenciaInterna = 1;
    }

    public Turno solicitarTurno(Paciente paciente, Area area) {
        // Genera el siguiente codigo segun letra de area (A-001, A-002, etc.).
        int siguiente = consecutivos.getOrDefault(area.getLetraIdentificadora(), 0) + 1;
        consecutivos.put(area.getLetraIdentificadora(), siguiente);

        String codigo = String.format("%c-%03d", area.getLetraIdentificadora(), siguiente);
        Turno nuevoTurno = new Turno(secuenciaInterna++, codigo, paciente, area);

        colaPorArea.computeIfAbsent(area.getId(), id -> new LinkedList<>()).addLast(nuevoTurno);
        turnoRepository.save(nuevoTurno);

        return nuevoTurno;
    }

    public Optional<Turno> llamarSiguiente(Area area, Box box) {
        // Atiende por orden de cola, salvo derivaciones prioritarias.
        LinkedList<Turno> cola = colaPorArea.getOrDefault(area.getId(), new LinkedList<>());
        if (cola.isEmpty()) {
            return Optional.empty();
        }

        if (turnoActivoPorBox.containsKey(box.getId())) {
            throw new BusinessException("El " + box + " ya tiene una atencion activa");
        }

        Turno turno = cola.removeFirst();
        turno.marcarLlamado(box);
        turno.iniciarAtencion();
        turnoActivoPorBox.put(box.getId(), turno);
        turnoRepository.update(turno);

        return Optional.of(turno);
    }

    public Turno derivarTurno(String codigoTurno, Area nuevaArea) {
        // Deriva el turno y lo pone al frente de la nueva cola.
        Turno turno = turnoRepository.findByCodigo(codigoTurno)
            .orElseThrow(() -> new BusinessException("No existe el turno: " + codigoTurno));

        if (turno.getEstado() == EstadoTurno.FINALIZADO) {
            throw new BusinessException("No se puede derivar un turno finalizado");
        }

        if (turno.getBoxAtencion() != null) {
            Turno activo = turnoActivoPorBox.get(turno.getBoxAtencion().getId());
            if (activo != null && activo.getCodigo().equalsIgnoreCase(turno.getCodigo())) {
                turnoActivoPorBox.remove(turno.getBoxAtencion().getId());
            }
            turno.liberarBoxAtencion();
        }

        String areaAnterior = turno.getAreaActual().getNombre();
        turno.derivar(nuevaArea, areaAnterior);

        colaPorArea.computeIfAbsent(nuevaArea.getId(), id -> new LinkedList<>()).addFirst(turno);
        turnoRepository.update(turno);

        return turno;
    }

    public List<Turno> listarEsperaPorArea(Area area) {
        return new ArrayList<>(colaPorArea.getOrDefault(area.getId(), new LinkedList<>()));
    }

    public List<Turno> listarHistorico() {
        return turnoRepository.findAll();
    }

    public Optional<Turno> obtenerTurnoActivo(Box box) {
        return Optional.ofNullable(turnoActivoPorBox.get(box.getId()));
    }

    public Turno cerrarAtencion(Box box) {
        // Cierra la atencion del box y libera ese box.
        Turno turno = turnoActivoPorBox.remove(box.getId());
        if (turno == null) {
            throw new BusinessException("No hay turno en atencion en " + box);
        }

        turno.finalizarAtencion();
        turnoRepository.update(turno);
        return turno;
    }
}
