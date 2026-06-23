package com.clinica.qms.observer;

import com.clinica.qms.model.Turno;
import com.clinica.qms.util.ArchivoLog;

/**
 * Implementacion del observer que persiste eventos en el archivo de auditoria.
 */
public class ArchivoLogObserver implements PantallaObserver {

    private final ArchivoLog log;

    public ArchivoLogObserver(ArchivoLog log) {
        this.log = log;
    }

    @Override
    public void onTurnoLlamado(Turno turno) {
        log.registrar("LLAMADO | " + turno.getDescripcionAuditoria()
                    + " | Box: " + turno.getBoxAtencion());
    }

    @Override
    public void onTurnoCerrado(Turno turno) {
        log.registrar("CERRADO | " + turno.getDescripcionAuditoria()
                    + " | Espera: " + turno.getEsperaEnSegundos() + "s"
                    + " | Atencion: " + turno.getAtencionEnSegundos() + "s");
    }

    @Override
    public void onTurnoDerivado(Turno turno, String areaDestino) {
        log.registrar("DERIVADO | " + turno.getCodigo()
                    + " -> " + areaDestino
                    + " | Origen: " + turno.getAreaOrigen());
    }
}
