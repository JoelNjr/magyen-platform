package com.magyen.platform.commercial.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del módulo comercial.
 */
public class QuotationDomainException extends RuntimeException {

    public QuotationDomainException(String message) {
        super(message);
    }
}
