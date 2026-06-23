package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Paciente;

import java.util.Optional;

/**
 * Interfaz especifica para el DAO de Paciente.
 */
public interface PacienteDao extends IDao<Paciente, Integer> {

    Optional<Paciente> buscarPorDni(String dni) throws DataAccessException;
}
