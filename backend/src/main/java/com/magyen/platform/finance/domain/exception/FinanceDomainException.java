package com.magyen.platform.finance.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del módulo financiero.
 */
public class FinanceDomainException extends RuntimeException {

    public FinanceDomainException(String message) {
        super(message);
    }
}
