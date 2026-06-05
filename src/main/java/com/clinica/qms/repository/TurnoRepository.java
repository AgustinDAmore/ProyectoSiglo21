package com.clinica.qms.repository;

import com.clinica.qms.model.Turno;

import java.util.List;
import java.util.Optional;

public interface TurnoRepository {
    // Guarda un turno nuevo.
    Turno save(Turno turno);

    // Actualiza un turno existente.
    void update(Turno turno);

    // Busca por codigo de turno (ejemplo A-001).
    Optional<Turno> findByCodigo(String codigo);

    // Devuelve todos los turnos guardados.
    List<Turno> findAll();
}
