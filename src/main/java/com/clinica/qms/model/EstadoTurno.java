package com.clinica.qms.model;

public enum EstadoTurno {
    // Estado inicial al crear el turno.
    EN_ESPERA,
    // El paciente ya fue llamado por pantalla.
    LLAMADO,
    // Se esta atendiendo en box.
    EN_ATENCION,
    // Atencion finalizada.
    FINALIZADO,
    // Turno enviado a otra area.
    DERIVADO
}
