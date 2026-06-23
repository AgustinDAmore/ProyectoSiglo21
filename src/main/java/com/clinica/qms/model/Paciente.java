package com.clinica.qms.model;

import java.time.LocalDateTime;

/** Paciente extiende Persona e implementa Auditable. */
public class Paciente extends Persona implements Auditable {

    private final String        dni;
    private final LocalDateTime fechaAlta;

    public Paciente(int id, String nombre, String apellido, String dni) {
        super(id, nombre, apellido);
        this.dni      = dni;
        this.fechaAlta = LocalDateTime.now();
    }

    @Override
    public String getRol() {
        return "PACIENTE";
    }

    public String getDni() {
        return dni;
    }

    @Override
    public String getDescripcionAuditoria() {
        return "Paciente DNI " + dni + " - " + nombre + " " + apellido;
    }

    @Override
    public LocalDateTime getFechaCreacion() {
        return fechaAlta;
    }
}
