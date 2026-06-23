package com.clinica.qms.model;

import java.time.LocalDateTime;

/**
 * Interfaz de auditoria.
 * Las entidades que la implementan pueden describirse a si mismas para logs.
 */
public interface Auditable {

    String getDescripcionAuditoria();

    LocalDateTime getFechaCreacion();
}
