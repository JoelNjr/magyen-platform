package com.magyen.platform.finance.domain.exception;

/**
 * Ya existe un período de nómina para el mismo empleado y periodStart.
 */
public class PayrollPeriodAlreadyExistsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE =
            "A payroll period already exists for this employee and period start.";

    public PayrollPeriodAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public PayrollPeriodAlreadyExistsException(String message) {
        super(message);
    }
}
