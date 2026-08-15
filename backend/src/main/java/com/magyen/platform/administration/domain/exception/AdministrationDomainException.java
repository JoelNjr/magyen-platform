package com.magyen.platform.administration.domain.exception;

/**
 * Excepción base para violaciones de reglas del módulo de administración.
 */
public class AdministrationDomainException extends RuntimeException {

    public AdministrationDomainException(String message) {
        super(message);
    }
}
