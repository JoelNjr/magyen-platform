package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Frecuencia de la compensación fija.
 * <p>
 * Los periodos nuevos cubren el mes calendario. {@link #BIWEEKLY} permanece
 * para empleados ya persistidos; la generación no abre un segundo periodo
 * dentro del mismo mes.
 */
public enum PayrollFrequency {

    MONTHLY,
    BIWEEKLY;

    public boolean participatesInFixedPayroll() {
        return this == MONTHLY || this == BIWEEKLY;
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
