package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Frecuencia de compensación fija.
 * <p>
 * Increment 10 solo soporta nómina quincenal (14 días).
 */
public enum PayrollFrequency {

    BIWEEKLY(14);

    private final int periodDays;

    PayrollFrequency(int periodDays) {
        this.periodDays = periodDays;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public static PayrollFrequency of(String value) {
        Objects.requireNonNull(value, "Payroll frequency must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Payroll frequency must not be blank");
        }
        try {
            return PayrollFrequency.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid payroll frequency: " + value);
        }
    }
}
