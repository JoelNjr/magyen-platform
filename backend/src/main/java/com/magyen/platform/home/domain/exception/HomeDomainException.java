package com.magyen.platform.home.domain.exception;

/**
 * Excepción base para violaciones de reglas del módulo Home (read model).
 */
public class HomeDomainException extends RuntimeException {

    public HomeDomainException(String message) {
        super(message);
    }
}
