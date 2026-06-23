package com.clinica.qms.dao;

import com.clinica.qms.exception.DataAccessException;
import com.clinica.qms.model.Paciente;

import java.util.ArrayList;
import java.util.Optional;

/** Implementacion en memoria del PacienteDao. */
public class PacienteDaoMemoriaImpl implements PacienteDao {

    private final ArrayList<Paciente> almacenamiento;
    private int secuencia;

    public PacienteDaoMemoriaImpl() {
        this.almacenamiento = new ArrayList<Paciente>();
        this.secuencia      = 1;
    }

    @Override
    public Optional<Paciente> buscarPorDni(String dni) throws DataAccessException {
        for (Paciente p : almacenamiento) {
            if (p.getDni().equals(dni)) return Optional.of(p);
        }
        return Optional.empty();
    }

    @Override
    public Paciente guardar(Paciente paciente) throws DataAccessException {
        Paciente con_id = new Paciente(secuencia++, paciente.getNombre(),
                                       paciente.getApellido(), paciente.getDni());
        almacenamiento.add(con_id);
        return con_id;
    }

    @Override
    public void actualizar(Paciente p) throws DataAccessException { /* inmutable */ }

    @Override
    public Optional<Paciente> buscarPorId(Integer id) throws DataAccessException {
        for (Paciente p : almacenamiento) {
            if (p.getId() == id) return Optional.of(p);
        }
        return Optional.empty();
    }

    @Override
    public ArrayList<Paciente> buscarTodos() throws DataAccessException {
        return new ArrayList<Paciente>(almacenamiento);
    }
}
