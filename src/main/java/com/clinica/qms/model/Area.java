package com.clinica.qms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Area de atencion de la clinica. Contiene un ArrayList de boxes. */
public class Area {

    private final int           id;
    private final String        nombre;
    private final char          letraIdentificadora;
    private final ArrayList<Box> boxes;

    public Area(int id, String nombre, char letraIdentificadora) {
        this.id                   = id;
        this.nombre               = nombre;
        this.letraIdentificadora  = letraIdentificadora;
        this.boxes                = new ArrayList<Box>();
    }

    public void agregarBox(Box box) {
        boxes.add(box);
    }

    public int    getId()                   { return id; }
    public String getNombre()               { return nombre; }
    public char   getLetraIdentificadora()  { return letraIdentificadora; }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

    @Override
    public String toString() {
        return nombre + " (" + letraIdentificadora + ")";
    }
}
