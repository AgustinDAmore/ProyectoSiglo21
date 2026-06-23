package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Turno;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Interfaz especifica para el DAO de Turno.
 * Extiende IDao agregando operaciones propias del dominio de turnos.
 */
public interface TurnoDao extends IDao<Turno, Integer> {

    /**
     * Genera el siguiente codigo de turno para un area de forma atomica.
     * MySQL: usa transaccion sobre consecutivo_turno.
     * Memoria: usa mapa interno de consecutivos.
     */
    String generarSiguienteCodigo(Area area) throws DataAccessException;

    /** Retorna los turnos en espera de un area, ordenados por prioridad DESC y creacion ASC. */
    ArrayList<Turno> buscarEnEsperaPorArea(int idArea) throws DataAccessException;

    /** Busca un turno por su codigo alfanumerico (ej. "A-001"). */
    Optional<Turno> buscarPorCodigo(String codigo) throws DataAccessException;
}
