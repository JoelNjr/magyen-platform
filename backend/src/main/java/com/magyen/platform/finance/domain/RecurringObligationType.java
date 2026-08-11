package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Clasificación de una {@link RecurringFinancialObligation}.
 * <p>
 * Solo tipifica la obligación. No implica agregados de nómina, crédito o servicios.
 */
public enum RecurringObligationType {

    SERVICE,
    PAYROLL,
    CREDIT,
    OTHER;

    public static RecurringObligationType of(String value) {
        Objects.requireNonNull(value, "Obligation type must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Obligation type must not be blank");
        }

        try {
            return RecurringObligationType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid obligation type: " + value);
        }
    }

    public static RecurringObligationType reconstitute(String value) {
        return of(value);
    }

    /**
     * Categoría tipada del ledger al pagar una ocurrencia de este tipo de obligación.
     */
    public FinancialCategory toExpenseCategory() {
        return switch (this) {
            case SERVICE -> FinancialCategory.SERVICES;
            case PAYROLL -> FinancialCategory.PAYROLL;
            case CREDIT -> FinancialCategory.CREDIT_PAYMENT;
            case OTHER -> FinancialCategory.OTHER_EXPENSE;
        };
    }
}
