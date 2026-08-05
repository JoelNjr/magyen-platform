package com.magyen.platform.production.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del módulo de producción.
 */
public class ProductionDomainException extends RuntimeException {

    public ProductionDomainException(String message) {
        super(message);
    }
}
