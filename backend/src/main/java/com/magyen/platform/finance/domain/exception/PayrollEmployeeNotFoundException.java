package com.magyen.platform.finance.domain.exception;

/**
 * El empleado de nómina no existe.
 */
public class PayrollEmployeeNotFoundException extends RuntimeException {

    public PayrollEmployeeNotFoundException(String message) {
        super(message);
    }
}
