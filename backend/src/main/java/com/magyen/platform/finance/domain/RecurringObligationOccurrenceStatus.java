package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Estado de una {@link RecurringFinancialObligationOccurrence}.
 */
public enum RecurringObligationOccurrenceStatus {

    PENDING,
    PAID,
    CANCELLED;

    public static RecurringObligationOccurrenceStatus of(String value) {
        Objects.requireNonNull(value, "Occurrence status must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Occurrence status must not be blank");
        }

        try {
            return RecurringObligationOccurrenceStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid occurrence status: " + value);
        }
    }

    public static RecurringObligationOccurrenceStatus reconstitute(String value) {
        return of(value);
    }
}
