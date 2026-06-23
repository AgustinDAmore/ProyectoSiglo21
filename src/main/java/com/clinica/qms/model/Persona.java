package com.clinica.qms.model;

/**
 * Clase abstracta base para personas del sistema.
 * Define el metodo template getRol() que cada subclase implementa.
 */
public abstract class Persona {

    protected final int    id;
    protected final String nombre;
    protected final String apellido;

    protected Persona(int id, String nombre, String apellido) {
        this.id      = id;
        this.nombre  = nombre;
        this.apellido = apellido;
    }

    /** Metodo abstracto: cada subclase define su rol en el sistema. */
    public abstract String getRol();

    public int    getId()      { return id; }
    public String getNombre()  { return nombre; }
    public String getApellido(){ return apellido; }

    @Override
    public String toString() {
        return "[" + getRol() + "] " + nombre + " " + apellido;
    }
}
