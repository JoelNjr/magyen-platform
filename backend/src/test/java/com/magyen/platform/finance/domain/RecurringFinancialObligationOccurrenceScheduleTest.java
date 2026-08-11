package com.magyen.platform.finance.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurringFinancialObligationOccurrenceScheduleTest {

    @Test
    void resolvesMonthlyDatesIncludingShortMonthClamp() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                31,
                LocalDate.of(2026, 1, 1),
                null,
                null,
                null
        );

        List<LocalDate> dates = obligation.resolveOccurrenceDueDates(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertEquals(List.of(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31)
        ), dates);
    }

    @Test
    void resolvesWeeklyDates() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Limpieza",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("50000.00")),
                RecurringObligationFrequency.WEEKLY,
                1,
                LocalDate.of(2026, 8, 3),
                null,
                null,
                null
        );

        List<LocalDate> dates = obligation.resolveOccurrenceDueDates(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        assertEquals(LocalDate.of(2026, 8, 3), dates.getFirst());
        assertTrue(dates.stream().allMatch(date -> date.getDayOfWeek().getValue() == 1));
        assertEquals(5, dates.size());
    }

    @Test
    void resolvesBiweeklyDatesEveryFourteenDays() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Nómina parcial",
                RecurringObligationType.PAYROLL,
                FinancialAmount.of(new BigDecimal("200000.00")),
                RecurringObligationFrequency.BIWEEKLY,
                1,
                LocalDate.of(2026, 8, 3),
                null,
                null,
                null
        );

        List<LocalDate> dates = obligation.resolveOccurrenceDueDates(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 30)
        );

        assertEquals(List.of(
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 28)
        ), dates);
    }

    @Test
    void resolvesYearlyAnniversary() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Licencia",
                RecurringObligationType.OTHER,
                FinancialAmount.of(new BigDecimal("500000.00")),
                RecurringObligationFrequency.YEARLY,
                15,
                LocalDate.of(2025, 8, 1),
                null,
                null,
                null
        );

        List<LocalDate> dates = obligation.resolveOccurrenceDueDates(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 12, 31)
        );

        assertEquals(List.of(
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2027, 8, 15)
        ), dates);
    }

    @Test
    void respectsStartAndEndBoundaries() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 10, 15),
                null,
                null
        );

        List<LocalDate> dates = obligation.resolveOccurrenceDueDates(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31)
        );

        assertEquals(List.of(
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 10, 15)
        ), dates);
        assertTrue(dates.contains(LocalDate.of(2026, 8, 15)));
        assertTrue(dates.contains(LocalDate.of(2026, 10, 15)));
    }
}
