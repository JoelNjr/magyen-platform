package com.magyen.platform.finance.domain.exception;

/**
 * Un período de nómina ya fue pagado; no puede pagarse de nuevo.
 */
public class PayrollPeriodAlreadyPaidException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "This payroll period is already paid.";

    public PayrollPeriodAlreadyPaidException() {
        super(DEFAULT_MESSAGE);
    }

    public PayrollPeriodAlreadyPaidException(String message) {
        super(message);
    }
}
