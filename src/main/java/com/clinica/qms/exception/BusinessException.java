package com.clinica.qms.exception;

public class BusinessException extends RuntimeException {
    // Error de reglas de negocio.
    public BusinessException(String message) {
        super(message);
    }
}
