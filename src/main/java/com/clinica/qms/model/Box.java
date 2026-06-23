package com.clinica.qms.model;

/** Puesto fisico de atencion dentro de un area. */
public class Box {

    private final int id;
    private final int numeroIdentificador;
    private final int idArea;

    public Box(int id, int numeroIdentificador, int idArea) {
        this.id                  = id;
        this.numeroIdentificador = numeroIdentificador;
        this.idArea              = idArea;
    }

    public int getId()                  { return id; }
    public int getNumeroIdentificador() { return numeroIdentificador; }
    public int getIdArea()              { return idArea; }

    @Override
    public String toString() {
        return "BOX-" + numeroIdentificador;
    }
}
