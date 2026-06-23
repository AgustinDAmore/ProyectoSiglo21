package com.clinica.qms.dao;

import com.clinica.qms.db.ConexionDB;
import com.clinica.qms.exception.DataAccessException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Clase abstracta base para todos los DAOs MySQL.
 * Aplica Template Method: define mapearResultSet() como abstracto.
 */
public abstract class AbstractDao<T, ID> implements IDao<T, ID> {

    protected final ConexionDB conexionDB;

    protected AbstractDao(ConexionDB conexionDB) {
        this.conexionDB = conexionDB;
    }

    protected Connection obtenerConexion() throws DataAccessException {
        return conexionDB.getConexion();
    }

    /**
     * Template Method: cada subclase transforma un ResultSet en su objeto de dominio.
     */
    protected abstract T mapearResultSet(ResultSet rs) throws SQLException;

    /**
     * Implementacion generica de buscarTodos usando el template method.
     * Las subclases pueden sobreescribirlo si necesitan un SQL diferente.
     */
    @Override
    public ArrayList<T> buscarTodos() throws DataAccessException {
        throw new UnsupportedOperationException("Implementar buscarTodos() en la subclase.");
    }

    @Override
    public Optional<T> buscarPorId(ID id) throws DataAccessException {
        throw new UnsupportedOperationException("Implementar buscarPorId() en la subclase.");
    }
}
