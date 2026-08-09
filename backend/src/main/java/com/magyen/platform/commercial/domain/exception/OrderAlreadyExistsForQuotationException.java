package com.magyen.platform.commercial.domain.exception;

/**
 * Indica que ya existe una Orden asociada a la Cotización solicitada.
 */
public class OrderAlreadyExistsForQuotationException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Ya existe una orden para esta cotización.";

    public OrderAlreadyExistsForQuotationException() {
        super(DEFAULT_MESSAGE);
    }

    public OrderAlreadyExistsForQuotationException(String message) {
        super(message);
    }
}
