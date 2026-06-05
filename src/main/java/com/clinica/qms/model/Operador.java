package com.clinica.qms.model;

public class Operador extends Persona {
    // Usuario del sistema que atiende turnos en un area.
    private final String usuario;
    private final Area areaAsignada;
    private Box boxSeleccionado;

    public Operador(int id, String nombre, String usuario, Area areaAsignada) {
        super(id, nombre);
        this.usuario = usuario;
        this.areaAsignada = areaAsignada;
    }

    public String getUsuario() {
        return usuario;
    }

    public Area getAreaAsignada() {
        return areaAsignada;
    }

    public Box getBoxSeleccionado() {
        return boxSeleccionado;
    }

    public void seleccionarBox(Box box) {
        this.boxSeleccionado = box;
    }
}
