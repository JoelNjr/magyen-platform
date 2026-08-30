package com.magyen.platform.finance.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaborPaymentWeekTest {

    @Test
    void usesMondayToSundayIsoWeek() {
        LaborPaymentWeek week = LaborPaymentWeek.of(LocalDate.of(2026, 8, 26));
        assertEquals(LocalDate.of(2026, 8, 24), week.getWeekStart());
        assertEquals(LocalDate.of(2026, 8, 30), week.getWeekEnd());
    }

    @Test
    void sameWeekSharesStableSourceId() {
        LaborPaymentWeek monday = LaborPaymentWeek.of(LocalDate.of(2026, 8, 24));
        LaborPaymentWeek sunday = LaborPaymentWeek.of(LocalDate.of(2026, 8, 30));
        assertEquals(monday.sourceId(), sunday.sourceId());
    }

    @Test
    void monthBoundaryKeepsWeekOfMonday() {
        LaborPaymentWeek week = LaborPaymentWeek.of(LocalDate.of(2026, 9, 2));
        assertEquals(LocalDate.of(2026, 8, 31), week.getWeekStart());
        assertEquals(LocalDate.of(2026, 9, 6), week.getWeekEnd());
    }
}
