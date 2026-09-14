package com.magyen.platform.home.application;

import com.magyen.platform.home.domain.exception.HomeDomainException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Resuelve el período calendario inclusive usado por lecturas Home.
 */
public final class HomePeriodResolver {

    private HomePeriodResolver() {
    }

    public static ResolvedPeriod resolve(LocalDate fromDate, LocalDate toDate, Clock clock) {
        Objects.requireNonNull(clock, "Clock must not be null");

        if (fromDate == null && toDate == null) {
            LocalDate today = LocalDate.now(clock);
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
            return new ResolvedPeriod(monthStart, monthEnd);
        }

        if (fromDate == null || toDate == null) {
            throw new HomeDomainException("Both fromDate and toDate must be provided together");
        }

        if (fromDate.isAfter(toDate)) {
            throw new HomeDomainException("From date must not be after to date");
        }

        return new ResolvedPeriod(fromDate, toDate);
    }

    public record ResolvedPeriod(LocalDate fromDate, LocalDate toDate) {
    }
}
