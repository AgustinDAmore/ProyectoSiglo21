package com.clinica.qms.model;

public class Box {
    // Box fisico donde se atiende al paciente.
    private final int id;
    private final int numeroIdentificador;
    private boolean disponible;

    public Box(int id, int numeroIdentificador) {
        this.id = id;
        this.numeroIdentificador = numeroIdentificador;
        this.disponible = true;
    }

    public int getId() {
        return id;
    }

    public int getNumeroIdentificador() {
        return numeroIdentificador;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "BOX-" + numeroIdentificador;
    }
}
