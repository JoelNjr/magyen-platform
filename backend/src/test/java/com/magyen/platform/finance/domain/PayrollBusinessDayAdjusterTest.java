package com.magyen.platform.finance.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollBusinessDayAdjusterTest {

    @Test
    void saturdayMovesToPreviousFriday() {
        assertEquals(
                LocalDate.of(2026, 8, 14),
                PayrollBusinessDayAdjuster.adjustToBusinessDay(LocalDate.of(2026, 8, 15))
        );
    }

    @Test
    void sundayMovesToNextMonday() {
        assertEquals(
                LocalDate.of(2026, 8, 17),
                PayrollBusinessDayAdjuster.adjustToBusinessDay(LocalDate.of(2026, 8, 16))
        );
    }

    @Test
    void mondayRemainsUnchanged() {
        LocalDate monday = LocalDate.of(2026, 8, 17);
        assertEquals(monday, PayrollBusinessDayAdjuster.adjustToBusinessDay(monday));
    }
}
