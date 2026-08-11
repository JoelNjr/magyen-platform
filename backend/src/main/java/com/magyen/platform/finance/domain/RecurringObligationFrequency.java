package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Locale;
import java.util.Objects;

/**
 * Frecuencia de recurrencia de una {@link RecurringFinancialObligation}.
 */
public enum RecurringObligationFrequency {

    WEEKLY(1, 7),
    BIWEEKLY(1, 14),
    MONTHLY(1, 31),
    YEARLY(1, 31);

    private final int minimumDueDay;
    private final int maximumDueDay;

    RecurringObligationFrequency(int minimumDueDay, int maximumDueDay) {
        this.minimumDueDay = minimumDueDay;
        this.maximumDueDay = maximumDueDay;
    }

    public int getMinimumDueDay() {
        return minimumDueDay;
    }

    public int getMaximumDueDay() {
        return maximumDueDay;
    }

    public static RecurringObligationFrequency of(String value) {
        Objects.requireNonNull(value, "Frequency must not be null");
        if (value.isBlank()) {
            throw new FinanceDomainException("Frequency must not be blank");
        }

        try {
            return RecurringObligationFrequency.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new FinanceDomainException("Invalid obligation frequency: " + value);
        }
    }

    public static RecurringObligationFrequency reconstitute(String value) {
        return of(value);
    }

    /**
     * Valida que {@code dueDay} sea apropiado para esta frecuencia, o {@code null}.
     */
    public void requireValidDueDay(Integer dueDay) {
        if (dueDay == null) {
            return;
        }
        if (dueDay < minimumDueDay || dueDay > maximumDueDay) {
            throw new FinanceDomainException(
                    "Due day for " + name() + " must be between "
                            + minimumDueDay + " and " + maximumDueDay
            );
        }
    }
}
