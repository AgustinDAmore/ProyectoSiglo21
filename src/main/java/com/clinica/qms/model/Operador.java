package com.clinica.qms.model;

/** Operador extiende Persona; tiene area y box asignados en sesion. */
public class Operador extends Persona {

    private final String usuario;
    private final Area   areaAsignada;
    private Box boxSeleccionado;

    public Operador(int id, String nombre, String apellido, String usuario, Area areaAsignada) {
        super(id, nombre, apellido);
        this.usuario      = usuario;
        this.areaAsignada = areaAsignada;
    }

    @Override
    public String getRol() {
        return "OPERADOR";
    }

    public String getUsuario()      { return usuario; }
    public Area   getAreaAsignada() { return areaAsignada; }

    public Box  getBoxSeleccionado() { return boxSeleccionado; }
    public void seleccionarBox(Box b){ this.boxSeleccionado = b; }
}
