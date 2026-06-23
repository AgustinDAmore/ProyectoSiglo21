package com.clinica.qms.model;

import java.time.Duration;
import java.time.LocalDateTime;

/** Entidad central del sistema. Implementa Auditable para el log. */
public class Turno implements Auditable {

    private int          id;          // no-final: el DAO lo asigna tras INSERT
    private final String codigo;
    private Paciente     paciente;
    private Area         areaActual;
    private EstadoTurno  estado;
    private int          prioridad;
    private LocalDateTime timestampCreacion;
    private LocalDateTime timestampLlamado;
    private LocalDateTime timestampInicioAten;
    private LocalDateTime timestampFinAten;
    private String        areaOrigen;
    private Box           boxAtencion;

    /** Constructor para nuevos turnos (id desconocido hasta persistir). */
    public Turno(String codigo, Paciente paciente, Area areaActual) {
        this.codigo            = codigo;
        this.paciente          = paciente;
        this.areaActual        = areaActual;
        this.estado            = EstadoTurno.EN_ESPERA;
        this.prioridad         = 0;
        this.timestampCreacion = LocalDateTime.now();
    }

    // -------- Metodos de negocio --------

    public void marcarLlamado(Box box) {
        this.estado          = EstadoTurno.LLAMADO;
        this.boxAtencion     = box;
        this.timestampLlamado = LocalDateTime.now();
    }

    public void iniciarAtencion() {
        this.estado             = EstadoTurno.EN_ATENCION;
        this.timestampInicioAten = LocalDateTime.now();
    }

    public void finalizarAtencion() {
        this.estado          = EstadoTurno.FINALIZADO;
        this.timestampFinAten = LocalDateTime.now();
    }

    /** Cierra tiempos de atencion sin cambiar estado (turno ya derivado a otra area). */
    public void registrarFinAtencion() {
        this.timestampFinAten = LocalDateTime.now();
    }

    public void derivar(Area nuevaArea, String areaOrigenNombre) {
        this.estado      = EstadoTurno.DERIVADO;
        this.areaActual  = nuevaArea;
        this.areaOrigen  = areaOrigenNombre;
        this.prioridad   = 1;
        this.boxAtencion = null;
    }

    // -------- Auditable --------

    @Override
    public String getDescripcionAuditoria() {
        return "Turno " + codigo + " | Estado: " + estado + " | Area: " + areaActual.getNombre();
    }

    @Override
    public LocalDateTime getFechaCreacion() {
        return timestampCreacion;
    }

    // -------- Metricas --------

    public long getEsperaEnSegundos() {
        LocalDateTime fin = (timestampLlamado != null) ? timestampLlamado : LocalDateTime.now();
        return Duration.between(timestampCreacion, fin).getSeconds();
    }

    public long getAtencionEnSegundos() {
        if (timestampInicioAten == null || timestampFinAten == null) return 0;
        return Duration.between(timestampInicioAten, timestampFinAten).getSeconds();
    }

    // -------- Setters (usados por DAO al cargar desde DB) --------

    public void setId(int id)                              { this.id = id; }
    public void setEstado(EstadoTurno e)                   { this.estado = e; }
    public void setPrioridad(int p)                        { this.prioridad = p; }
    public void setAreaOrigen(String ao)                   { this.areaOrigen = ao; }
    public void setBoxAtencion(Box b)                      { this.boxAtencion = b; }
    public void setTimestampLlamado(LocalDateTime t)       { this.timestampLlamado = t; }
    public void setTimestampCreacion(LocalDateTime t)      { this.timestampCreacion = t; }
    public void setTimestampInicioAten(LocalDateTime t)    { this.timestampInicioAten = t; }
    public void setTimestampFinAten(LocalDateTime t)       { this.timestampFinAten = t; }

    // -------- Getters --------

    public int          getId()                   { return id; }
    public String       getCodigo()               { return codigo; }
    public Paciente     getPaciente()             { return paciente; }
    public Area         getAreaActual()           { return areaActual; }
    public EstadoTurno  getEstado()               { return estado; }
    public int          getPrioridad()            { return prioridad; }
    public LocalDateTime getTimestampCreacion()   { return timestampCreacion; }
    public LocalDateTime getTimestampLlamado()    { return timestampLlamado; }
    public LocalDateTime getTimestampInicioAten() { return timestampInicioAten; }
    public LocalDateTime getTimestampFinAten()  { return timestampFinAten; }
    public String       getAreaOrigen()           { return areaOrigen; }
    public Box          getBoxAtencion()          { return boxAtencion; }

    @Override
    public String toString() {
        return codigo + " | " + estado + " | " + areaActual.getNombre() + " | Prio: " + prioridad;
    }
}
