package com.clinica.qms.model;

public class Paciente extends Persona {
    // Se usa DNI como dato identificatorio principal.
    private final String dni;

    public Paciente(int id, String nombre, String dni) {
        super(id, nombre);
        this.dni = dni;
    }

    public String getDni() {
        return dni;
    }
}
