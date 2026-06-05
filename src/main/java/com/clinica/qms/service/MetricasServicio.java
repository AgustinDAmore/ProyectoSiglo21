package com.clinica.qms.service;

import com.clinica.qms.model.Turno;

import java.util.List;

public class MetricasServicio {

    public double calcularEsperaPromedioSegundos(List<Turno> turnos) {
        // Promedio de espera considerando todos los turnos.
        return turnos.stream()
            .mapToLong(Turno::getEsperaEnSegundos)
            .average()
            .orElse(0.0);
    }

    public double calcularAtencionPromedioSegundos(List<Turno> turnos) {
        // Solo toma turnos con atencion finalizada (tiempo > 0).
        return turnos.stream()
            .mapToLong(Turno::getAtencionEnSegundos)
            .filter(v -> v > 0)
            .average()
            .orElse(0.0);
    }
}
