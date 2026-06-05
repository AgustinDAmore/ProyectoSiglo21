package com.clinica.qms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Area {
    // Representa un sector de atencion de la clinica.
    private final int id;
    private final String nombre;
    private final char letraIdentificadora;
    private final List<Box> boxes;

    public Area(int id, String nombre, char letraIdentificadora) {
        this.id = id;
        this.nombre = nombre;
        this.letraIdentificadora = letraIdentificadora;
        this.boxes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public char getLetraIdentificadora() {
        return letraIdentificadora;
    }

    public void agregarBox(Box box) {
        boxes.add(box);
    }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }
}
