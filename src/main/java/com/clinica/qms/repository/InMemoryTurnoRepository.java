package com.clinica.qms.repository;

import com.clinica.qms.model.Turno;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTurnoRepository implements TurnoRepository {
    // Repositorio simple para pruebas o ejecucion local sin DB.
    private final List<Turno> almacenamiento = new ArrayList<>();

    @Override
    public Turno save(Turno turno) {
        almacenamiento.add(turno);
        return turno;
    }

    @Override
    public void update(Turno turno) {
        // En memoria no hace falta reemplazo explicito porque el objeto es mutable.
    }

    @Override
    public Optional<Turno> findByCodigo(String codigo) {
        return almacenamiento.stream()
            .filter(t -> t.getCodigo().equalsIgnoreCase(codigo))
            .findFirst();
    }

    @Override
    public List<Turno> findAll() {
        return new ArrayList<>(almacenamiento);
    }
}
