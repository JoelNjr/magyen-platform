package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Estado de un {@link PayrollDeduction}.
 * <p>
 * {@link #CANCELLED} conserva historia y no entra en totales activos.
 */
public enum PayrollDeductionStatus {

    ACTIVE,
    CANCELLED;

    public static PayrollDeductionStatus of(String value) {
        Objects.requireNonNull(value, "Payroll deduction status must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Payroll deduction status must not be blank");
        }
        try {
            return PayrollDeductionStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid payroll deduction status: " + value);
        }
    }
}
