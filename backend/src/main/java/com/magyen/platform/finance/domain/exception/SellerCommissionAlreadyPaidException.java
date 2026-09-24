package com.magyen.platform.finance.domain.exception;

/**
 * La comisión de ese vendedor y mes ya fue pagada.
 */
public class SellerCommissionAlreadyPaidException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "This seller commission month is already paid.";

    public SellerCommissionAlreadyPaidException() {
        super(DEFAULT_MESSAGE);
    }

    public SellerCommissionAlreadyPaidException(String message) {
        super(message);
    }
}
