package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurringFinancialObligationTest {

    @Test
    void createsValidMonthlyObligation() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                "Internet del taller",
                null
        );

        assertTrue(obligation.isActive());
        assertEquals(RecurringObligationFrequency.MONTHLY, obligation.getFrequency());
        assertEquals(15, obligation.getDueDay());
        assertEquals(new BigDecimal("120000.00"), obligation.getExpectedAmount().getValue());
        assertNull(obligation.getEndDate());
    }

    @Test
    void createsValidYearlyObligation() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Licencia anual",
                RecurringObligationType.OTHER,
                FinancialAmount.of(new BigDecimal("500000.00")),
                RecurringObligationFrequency.YEARLY,
                1,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 12, 31),
                null,
                null
        );

        assertEquals(RecurringObligationFrequency.YEARLY, obligation.getFrequency());
        assertEquals(LocalDate.of(2027, 12, 31), obligation.getEndDate());
    }

    @Test
    void rejectsInvalidAmount() {
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(BigDecimal.ZERO));
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(new BigDecimal("-10.00")));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(FinanceDomainException.class, () -> RecurringFinancialObligation.create(
                "  ",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("100.00")),
                RecurringObligationFrequency.MONTHLY,
                10,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        ));
    }

    @Test
    void rejectsInvalidFrequency() {
        assertThrows(FinanceDomainException.class, () -> RecurringObligationFrequency.of("DAILY"));
    }

    @Test
    void rejectsInvalidDueDay() {
        assertThrows(FinanceDomainException.class, () -> RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("100.00")),
                RecurringObligationFrequency.MONTHLY,
                32,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        ));

        assertThrows(FinanceDomainException.class, () -> RecurringFinancialObligation.create(
                "Semanal",
                RecurringObligationType.PAYROLL,
                FinancialAmount.of(new BigDecimal("100.00")),
                RecurringObligationFrequency.WEEKLY,
                8,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        ));
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(FinanceDomainException.class, () -> RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("100.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 1),
                null,
                null
        ));
    }

    @Test
    void deactivatesAndReactivates() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        );

        obligation.deactivate();
        assertFalse(obligation.isActive());

        obligation.activate();
        assertTrue(obligation.isActive());
    }

    @Test
    void updateChangesFieldsWithoutChangingActiveState() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        );
        obligation.deactivate();

        obligation.update(
                "Internet fibra",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("130000.00")),
                RecurringObligationFrequency.MONTHLY,
                20,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2027, 8, 1),
                "Actualizado",
                "Obs"
        );

        assertEquals("Internet fibra", obligation.getName());
        assertEquals(new BigDecimal("130000.00"), obligation.getExpectedAmount().getValue());
        assertEquals(20, obligation.getDueDay());
        assertFalse(obligation.isActive());
    }
}
