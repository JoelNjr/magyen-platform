package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Tipo controlado de un descuento de nómina.
 * <p>
 * No implica impuestos, cuotas, interés ni asiento del ledger.
 */
public enum PayrollDeductionType {

    LOAN,
    ADVANCE,
    OTHER;

    public static PayrollDeductionType of(String value) {
        Objects.requireNonNull(value, "Payroll deduction type must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Payroll deduction type must not be blank");
        }
        try {
            return PayrollDeductionType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid payroll deduction type: " + value);
        }
    }
}
