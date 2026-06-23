package com.clinica.qms.service;

import com.clinica.qms.model.EstadoTurno;
import com.clinica.qms.model.Turno;

import java.util.ArrayList;

/**
 * Servicio de metricas del sistema.
 * generarReporte() retorna String[] con el resumen de metricas.
 */
public class MetricasServicio {

    /** Calcula el tiempo de espera promedio en segundos. */
    public double calcularEsperaPromedio(ArrayList<Turno> turnos) {
        if (turnos == null || turnos.isEmpty()) return 0.0;
        long total = 0;
        int  count = 0;
        for (Turno t : turnos) {
            if (t.getTimestampLlamado() != null) {
                total += t.getEsperaEnSegundos();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) total / count;
    }

    /** Calcula el tiempo de atencion promedio en segundos. */
    public double calcularAtencionPromedio(ArrayList<Turno> turnos) {
        if (turnos == null || turnos.isEmpty()) return 0.0;
        long total = 0;
        int  count = 0;
        for (Turno t : turnos) {
            if (t.getAtencionEnSegundos() > 0) {
                total += t.getAtencionEnSegundos();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) total / count;
    }

    /**
     * Genera un reporte resumido como array de Strings.
     * [0] Total atendidos, [1] Espera prom., [2] Atencion prom., [3] Total derivados.
     */
    public String[] generarReporte(ArrayList<Turno> turnos) {
        String[] reporte = new String[4];
        int atendidos = 0;
        int derivados = 0;
        for (Turno t : turnos) {
            // Atendido = se cerro la atencion (completa o antes de derivar)
            if (t.getEstado() == EstadoTurno.FINALIZADO || t.getTimestampFinAten() != null) {
                atendidos++;
            }
            if (t.getEstado() == EstadoTurno.DERIVADO || t.getAreaOrigen() != null) {
                derivados++;
            }
        }
        reporte[0] = "Turnos atendidos: " + atendidos;
        reporte[1] = String.format("Espera promedio: %.1f seg", calcularEsperaPromedio(turnos));
        reporte[2] = String.format("Atencion promedio: %.1f seg", calcularAtencionPromedio(turnos));
        reporte[3] = "Turnos derivados: " + derivados;
        return reporte;
    }
}
