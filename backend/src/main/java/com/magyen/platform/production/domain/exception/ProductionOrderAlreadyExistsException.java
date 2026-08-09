package com.magyen.platform.production.domain.exception;

/**
 * Indica que ya existe una Orden de Producción asociada a la Orden comercial solicitada.
 */
public class ProductionOrderAlreadyExistsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "Ya existe una orden de producción para esta orden comercial.";

    public ProductionOrderAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public ProductionOrderAlreadyExistsException(String message) {
        super(message);
    }
}
