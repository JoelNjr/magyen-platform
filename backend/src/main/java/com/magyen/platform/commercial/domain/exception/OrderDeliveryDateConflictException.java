package com.magyen.platform.commercial.domain.exception;

/**
 * La fecha enviada no coincide con la entrega ya registrada.
 * No reescribe {@code actualDeliveryDate}.
 */
public class OrderDeliveryDateConflictException extends RuntimeException {

    public OrderDeliveryDateConflictException(String message) {
        super(message);
    }
}
