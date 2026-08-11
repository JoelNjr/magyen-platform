package com.magyen.platform.production.domain.exception;

/**
 * Un registro de mano de obra ya fue pagado; no puede pagarse de nuevo.
 */
public class ProductionLaborWorkAlreadyPaidException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "This production labor work is already paid.";

    public ProductionLaborWorkAlreadyPaidException() {
        super(DEFAULT_MESSAGE);
    }

    public ProductionLaborWorkAlreadyPaidException(String message) {
        super(message);
    }
}
