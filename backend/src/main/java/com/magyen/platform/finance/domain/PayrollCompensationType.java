package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Modelo de compensación laboral de un empleado de nómina.
 * <p>
 * {@link #FIXED_PAYROLL} participa en generación biweekly de períodos.
 * {@link #PRODUCTION_BASED} existe en el modelo pero no entra en nómina fija.
 */
public enum PayrollCompensationType {

    FIXED_PAYROLL,
    PRODUCTION_BASED;

    public static PayrollCompensationType of(String value) {
        Objects.requireNonNull(value, "Compensation type must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Compensation type must not be blank");
        }
        try {
            return PayrollCompensationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid payroll compensation type: " + value);
        }
    }
}
