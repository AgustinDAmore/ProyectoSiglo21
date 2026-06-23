package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Interfaz generica de acceso a datos (patron DAO).
 * T = tipo de entidad, ID = tipo de clave primaria.
 */
public interface IDao<T, ID> {

    T guardar(T entidad) throws DataAccessException;

    void actualizar(T entidad) throws DataAccessException;

    Optional<T> buscarPorId(ID id) throws DataAccessException;

    ArrayList<T> buscarTodos() throws DataAccessException;
}
