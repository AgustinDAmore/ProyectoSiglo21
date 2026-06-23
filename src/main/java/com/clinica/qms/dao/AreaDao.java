package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Area;
import com.clinica.qms.model.Box;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Interfaz especifica para el DAO de Area.
 */
public interface AreaDao extends IDao<Area, Integer> {

    Optional<Area> buscarPorLetra(char letra) throws DataAccessException;

    /** Carga areas con sus boxes incluidos. */
    ArrayList<Area> buscarTodasConBoxes() throws DataAccessException;
}
