package com.clinica.qms.exception;

public class DataAccessException extends RuntimeException {
    // Error al leer/escribir datos en persistencia.
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
