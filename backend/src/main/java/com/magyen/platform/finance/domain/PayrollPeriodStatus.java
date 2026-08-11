package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Estado de un período de nómina fija.
 */
public enum PayrollPeriodStatus {

    PENDING,
    PAID,
    CANCELLED;

    public static PayrollPeriodStatus of(String value) {
        Objects.requireNonNull(value, "Payroll period status must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Payroll period status must not be blank");
        }
        try {
            return PayrollPeriodStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid payroll period status: " + value);
        }
    }
}
