package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollDeductionTest {

    @Test
    void createStartsActiveAndDoesNotAllowZeroOrNegativeAmount() {
        PayrollDeduction deduction = PayrollDeduction.create(
                UUID.randomUUID(),
                PayrollDeductionType.LOAN,
                FinancialAmount.of(new BigDecimal("150000.00")),
                LocalDate.of(2026, 8, 17),
                "  Préstamo autorizado  ",
                LocalDateTime.of(2026, 8, 17, 10, 0)
        );

        assertEquals(PayrollDeductionStatus.ACTIVE, deduction.getStatus());
        assertEquals("Préstamo autorizado", deduction.getDescription());
        assertTrue(deduction.isActive());

        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(BigDecimal.ZERO));
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(new BigDecimal("-10.00")));
    }

    @Test
    void cancelPreservesIdentityAndRejectsSecondCancel() {
        PayrollDeduction deduction = PayrollDeduction.create(
                UUID.randomUUID(),
                PayrollDeductionType.ADVANCE,
                FinancialAmount.of(new BigDecimal("50000.00")),
                LocalDate.of(2026, 8, 17),
                null,
                LocalDateTime.of(2026, 8, 17, 10, 0)
        );
        UUID originalId = deduction.getId();

        deduction.cancel();

        assertEquals(originalId, deduction.getId());
        assertEquals(PayrollDeductionStatus.CANCELLED, deduction.getStatus());
        assertFalse(deduction.isActive());
        assertThrows(FinanceDomainException.class, deduction::cancel);
    }
}
