package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollMonthlyPeriodTest {

    @Test
    void resolvesFullMonthsIncludingThirtyThirtyOneAndLeapFebruary() {
        PayrollEmployee employee = PayrollEmployee.createFixed(
                "Mensual",
                FinancialAmount.of(new BigDecimal("1500000.00")),
                LocalDate.of(2026, 1, 1),
                null
        );

        assertMonth(employee, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        assertMonth(employee, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        assertMonth(employee, LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
        assertMonth(employee, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(PayrollFrequency.MONTHLY, employee.getFrequency());
    }

    @Test
    void rejectsPartialFortnightRanges() {
        FinancialAmount amount = FinancialAmount.of(new BigDecimal("1500000.00"));
        assertThrows(FinanceDomainException.class, () -> PayrollPeriod.createPending(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 9, 15),
                amount
        ));
        assertThrows(FinanceDomainException.class, () -> PayrollPeriod.createPending(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 16),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 9, 30),
                amount
        ));
    }

    @Test
    void oneMonthProducesOneWindowWithTheFixedAmountOnce() {
        PayrollEmployee employee = PayrollEmployee.createFixed(
                "Una vez",
                FinancialAmount.of(new BigDecimal("1500000.00")),
                LocalDate.of(2026, 9, 15),
                null
        );

        List<PayrollEmployee.ResolvedPayrollPeriodWindow> windows = employee.resolveMonthlyPeriodWindows(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        );

        assertEquals(1, windows.size());
        assertEquals(LocalDate.of(2026, 9, 1), windows.getFirst().periodStart());
        assertEquals(LocalDate.of(2026, 9, 30), windows.getFirst().periodEnd());
    }

    @Test
    void reconstitutesHistoricalFortnightAndLegacyFrequency() {
        PayrollPeriod historical = PayrollPeriod.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 8, 28),
                FinancialAmount.of(new BigDecimal("1500000.00")),
                PayrollPeriodStatus.PENDING,
                null,
                null,
                null
        );
        assertEquals(LocalDate.of(2026, 8, 15), historical.getPeriodStart());

        PayrollEmployee legacy = PayrollEmployee.reconstitute(
                UUID.randomUUID(),
                "Legado",
                true,
                PayrollCompensationType.FIXED_PAYROLL,
                FinancialAmount.of(new BigDecimal("1500000.00")),
                PayrollFrequency.BIWEEKLY,
                LocalDate.of(2026, 8, 1),
                null,
                false
        );
        List<PayrollEmployee.ResolvedPayrollPeriodWindow> windows = legacy.resolveMonthlyPeriodWindows(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 30)
        );
        assertEquals(2, windows.size());
        assertEquals(LocalDate.of(2026, 8, 1), windows.getFirst().periodStart());
        assertEquals(LocalDate.of(2026, 8, 31), windows.getFirst().periodEnd());
    }

    private static void assertMonth(PayrollEmployee employee, LocalDate start, LocalDate end) {
        List<PayrollEmployee.ResolvedPayrollPeriodWindow> windows = employee.resolveMonthlyPeriodWindows(start, end);
        assertEquals(1, windows.size());
        assertEquals(start, windows.getFirst().periodStart());
        assertEquals(end, windows.getFirst().periodEnd());
        PayrollPeriod.createPending(
                employee.getId(),
                start,
                end,
                windows.getFirst().expectedPaymentDate(),
                employee.getFixedAmount()
        );
    }
}
