package com.clinica.qms.observer;

import com.clinica.qms.model.Turno;

/**
 * Interfaz para observers de pantalla.
 * Implementada por cualquier componente que desee recibir eventos del GestorTurnos.
 */
public interface PantallaObserver {

    void onTurnoLlamado(Turno turno);

    void onTurnoCerrado(Turno turno);

    void onTurnoDerivado(Turno turno, String areaDestino);
}
