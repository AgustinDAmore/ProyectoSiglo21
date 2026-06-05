package com.clinica.qms.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Turno {
    // Entidad central del sistema de gestion de turnos.
    private final int id;
    private final String codigo;
    private final Paciente paciente;
    private Area areaActual;
    private EstadoTurno estado;
    private int prioridad;
    private LocalDateTime timestampCreacion;
    private LocalDateTime timestampLlamado;
    private LocalDateTime timestampInicioAtencion;
    private LocalDateTime timestampFinAtencion;
    private String areaOrigen;
    private Box boxAtencion;

    public Turno(int id, String codigo, Paciente paciente, Area areaActual) {
        this.id = id;
        this.codigo = codigo;
        this.paciente = paciente;
        this.areaActual = areaActual;
        this.estado = EstadoTurno.EN_ESPERA;
        this.prioridad = 0;
        this.timestampCreacion = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Area getAreaActual() {
        return areaActual;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public LocalDateTime getTimestampCreacion() {
        return timestampCreacion;
    }

    public LocalDateTime getTimestampLlamado() {
        return timestampLlamado;
    }

    public LocalDateTime getTimestampInicioAtencion() {
        return timestampInicioAtencion;
    }

    public LocalDateTime getTimestampFinAtencion() {
        return timestampFinAtencion;
    }

    public String getAreaOrigen() {
        return areaOrigen;
    }

    public Box getBoxAtencion() {
        return boxAtencion;
    }

    public void liberarBoxAtencion() {
        this.boxAtencion = null;
    }

    public void marcarLlamado(Box box) {
        // Marca el momento en que se llama al paciente.
        this.estado = EstadoTurno.LLAMADO;
        this.boxAtencion = box;
        this.timestampLlamado = LocalDateTime.now();
    }

    public void iniciarAtencion() {
        // Marca inicio de la atencion en box.
        this.estado = EstadoTurno.EN_ATENCION;
        this.timestampInicioAtencion = LocalDateTime.now();
    }

    public void finalizarAtencion() {
        // Marca fin de atencion para metricas.
        this.estado = EstadoTurno.FINALIZADO;
        this.timestampFinAtencion = LocalDateTime.now();
    }

    public void derivar(Area nuevaArea, String areaOrigen) {
        // Derivacion con prioridad para ser atendido antes.
        this.estado = EstadoTurno.DERIVADO;
        this.areaActual = nuevaArea;
        this.areaOrigen = areaOrigen;
        this.prioridad = 1;
    }

    // Permite reconstruir el estado del turno desde la base de datos.
    public void rehidratarDesdePersistencia(
        EstadoTurno estado,
        int prioridad,
        LocalDateTime timestampCreacion,
        LocalDateTime timestampLlamado,
        LocalDateTime timestampInicioAtencion,
        LocalDateTime timestampFinAtencion,
        String areaOrigen,
        Box boxAtencion,
        Area areaActual
    ) {
        this.estado = estado;
        this.prioridad = prioridad;
        this.timestampCreacion = timestampCreacion;
        this.timestampLlamado = timestampLlamado;
        this.timestampInicioAtencion = timestampInicioAtencion;
        this.timestampFinAtencion = timestampFinAtencion;
        this.areaOrigen = areaOrigen;
        this.boxAtencion = boxAtencion;
        this.areaActual = areaActual;
    }

    public long getEsperaEnSegundos() {
        // Si no fue llamado, calcula espera hasta ahora.
        LocalDateTime finEspera = timestampLlamado != null ? timestampLlamado : LocalDateTime.now();
        return Duration.between(timestampCreacion, finEspera).getSeconds();
    }

    public long getAtencionEnSegundos() {
        if (timestampInicioAtencion == null || timestampFinAtencion == null) {
            return 0;
        }
        return Duration.between(timestampInicioAtencion, timestampFinAtencion).getSeconds();
    }

    @Override
    public String toString() {
        return codigo + " | " + estado + " | Area: " + areaActual.getNombre() + " | Prioridad: " + prioridad;
    }
}
