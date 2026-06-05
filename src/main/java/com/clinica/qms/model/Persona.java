package com.clinica.qms.model;

public abstract class Persona {
    // Clase base comun para paciente y operador.
    private final int id;
    private final String nombre;

    protected Persona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
