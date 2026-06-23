package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Turno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacion en memoria del TurnoDao.
 * Usada en modo demo cuando MySQL no esta disponible.
 * NO extiende AbstractDao (no requiere ConexionDB).
 */
public class TurnoDaoMemoriaImpl implements TurnoDao {

    private final ArrayList<Turno>       almacenamiento;
    private final Map<Character, Integer> consecutivos;
    private int secuencia;

    public TurnoDaoMemoriaImpl() {
        this.almacenamiento = new ArrayList<Turno>();
        this.consecutivos   = new HashMap<Character, Integer>();
        this.secuencia      = 1;
    }

    @Override
    public String generarSiguienteCodigo(Area area) throws DataAccessException {
        int siguiente = consecutivos.getOrDefault(area.getLetraIdentificadora(), 0) + 1;
        consecutivos.put(area.getLetraIdentificadora(), siguiente);
        return String.format("%c-%03d", area.getLetraIdentificadora(), siguiente);
    }

    @Override
    public Turno guardar(Turno turno) throws DataAccessException {
        turno.setId(secuencia++);
        almacenamiento.add(turno);
        return turno;
    }

    @Override
    public void actualizar(Turno turno) throws DataAccessException {
        // Objetos mutables: el cambio ya esta reflejado en la lista.
    }

    @Override
    public Optional<Turno> buscarPorCodigo(String codigo) throws DataAccessException {
        for (Turno t : almacenamiento) {
            if (t.getCodigo().equalsIgnoreCase(codigo)) return Optional.of(t);
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Turno> buscarEnEsperaPorArea(int idArea) throws DataAccessException {
        ArrayList<Turno> resultado = new ArrayList<Turno>();
        for (Turno t : almacenamiento) {
            boolean esArea    = t.getAreaActual().getId() == idArea;
            boolean enEspera  = t.getEstado() == EstadoTurno.EN_ESPERA
                             || t.getEstado() == EstadoTurno.DERIVADO;
            if (esArea && enEspera) resultado.add(t);
        }
        return resultado;
    }

    @Override
    public ArrayList<Turno> buscarTodos() throws DataAccessException {
        return new ArrayList<Turno>(almacenamiento);
    }

    @Override
    public Optional<Turno> buscarPorId(Integer id) throws DataAccessException {
        for (Turno t : almacenamiento) {
            if (t.getId() == id) return Optional.of(t);
        }
        return Optional.empty();
    }
}
