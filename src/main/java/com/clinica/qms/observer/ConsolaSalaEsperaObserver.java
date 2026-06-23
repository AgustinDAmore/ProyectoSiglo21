package com.clinica.qms.observer;

import com.clinica.qms.model.Turno;
import com.clinica.qms.util.ConsolaUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Implementacion del observer que muestra llamados en pantalla de consola.
 * Mantiene un historial de los ultimos llamados para la pantalla de sala de espera.
 */
public class ConsolaSalaEsperaObserver implements PantallaObserver {

    private static final int CAPACIDAD_HISTORIAL = 5;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ArrayList<String> ultimosLlamados;

    public ConsolaSalaEsperaObserver() {
        this.ultimosLlamados = new ArrayList<String>();
    }

    @Override
    public void onTurnoLlamado(Turno turno) {
        String hora  = turno.getTimestampLlamado() != null
                       ? turno.getTimestampLlamado().format(FORMATO)
                       : "";
        String aviso = "==> TURNO " + turno.getCodigo()
                     + " dirijase a " + turno.getBoxAtencion()
                     + " (" + turno.getAreaActual().getNombre() + ")  " + hora;
        System.out.println();
        ConsolaUtil.println(ConsolaUtil.VERDE + ConsolaUtil.NEGRITA,
                "┌─────────────────────────────────────────────┐");
        ConsolaUtil.println(ConsolaUtil.VERDE + ConsolaUtil.NEGRITA,
                "│  LLAMADO: " + aviso);
        ConsolaUtil.println(ConsolaUtil.VERDE + ConsolaUtil.NEGRITA,
                "└─────────────────────────────────────────────┘");

        agregarAlHistorial("TURNO " + turno.getCodigo() + " -> " + turno.getBoxAtencion());
    }

    @Override
    public void onTurnoCerrado(Turno turno) {
        // Solo logging silencioso; el operador ya ve el resultado en su menu.
    }

    @Override
    public void onTurnoDerivado(Turno turno, String areaDestino) {
        ConsolaUtil.println(ConsolaUtil.AMARILLO + ConsolaUtil.NEGRITA,
                "[DERIVADO] Turno " + turno.getCodigo()
                + " -> " + areaDestino + " (prioridad alta)");
    }

    private void agregarAlHistorial(String linea) {
        if (ultimosLlamados.size() >= CAPACIDAD_HISTORIAL) {
            ultimosLlamados.remove(0);
        }
        ultimosLlamados.add(linea);
    }

    public ArrayList<String> getUltimosLlamados() {
        return new ArrayList<String>(ultimosLlamados);
    }
}
