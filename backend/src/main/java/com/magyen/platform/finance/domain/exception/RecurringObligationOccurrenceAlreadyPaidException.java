package com.magyen.platform.finance.domain.exception;

/**
 * Indica que la ocurrencia ya fue pagada y no puede generar otro movimiento del ledger.
 */
public class RecurringObligationOccurrenceAlreadyPaidException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "This recurring obligation occurrence is already paid.";

    public RecurringObligationOccurrenceAlreadyPaidException() {
        super(DEFAULT_MESSAGE);
    }

    public RecurringObligationOccurrenceAlreadyPaidException(String message) {
        super(message);
    }
}
