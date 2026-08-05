package com.magyen.platform.commercial.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio del agregado Order.
 */
public class OrderDomainException extends RuntimeException {

    public OrderDomainException(String message) {
        super(message);
    }
}
