package com.magyen.platform.finance.domain.exception;

/**
 * Indica que ya existe una ocurrencia para la misma obligación y fecha de vencimiento.
 */
public class RecurringObligationOccurrenceAlreadyExistsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "An occurrence already exists for this recurring obligation and due date.";

    public RecurringObligationOccurrenceAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public RecurringObligationOccurrenceAlreadyExistsException(String message) {
        super(message);
    }
}
