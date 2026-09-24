package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SellerCommissionSettlementTest {

    @Test
    void septemberSettlementFreezesThePaidSnapshot() {
        UUID settlementId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        LocalDateTime paidAt = LocalDateTime.of(2026, 9, 30, 10, 0);

        SellerCommissionSettlement settlement = SellerCommissionSettlement.createPaid(
                settlementId,
                employeeId,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                FinancialAmount.of(new BigDecimal("15284000.00")),
                7,
                FinancialAmount.of(new BigDecimal("764200.00")),
                LocalDate.of(2026, 9, 30),
                paidAt,
                transactionId,
                "pago septiembre"
        );

        assertEquals(SellerCommissionSettlementStatus.PAID, settlement.getStatus());
        assertEquals(employeeId, settlement.getEmployeeId());
        assertEquals(LocalDate.of(2026, 9, 1), settlement.getPeriodStart());
        assertEquals(new BigDecimal("764200.00"), settlement.getCommissionSnapshot().getValue());
        assertEquals(7, settlement.getOrderCountSnapshot());
        assertEquals(transactionId, settlement.getFinancialTransactionId());
        assertEquals(paidAt, settlement.getPaidAt());
    }

    @Test
    void augustAndPartialPeriodsAreRejected() {
        assertThrows(FinanceDomainException.class, () ->
                SellerCommissionSettlement.requirePayableMonth(LocalDate.of(2026, 8, 1)));
        assertThrows(FinanceDomainException.class, () ->
                SellerCommissionSettlement.periodEndOf(LocalDate.of(2026, 9, 15)));
        assertThrows(FinanceDomainException.class, () -> paid(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                1,
                "100.00"
        ));
    }

    @Test
    void aPaidSettlementRequiresOrdersAndAFinancialTransaction() {
        assertThrows(FinanceDomainException.class, () ->
                FinancialAmount.of(BigDecimal.ZERO));
        assertThrows(FinanceDomainException.class, () -> paid(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                0,
                "100.00"
        ));
        assertThrows(NullPointerException.class, () -> SellerCommissionSettlement.createPaid(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                FinancialAmount.of(new BigDecimal("100.00")),
                1,
                FinancialAmount.of(new BigDecimal("5.00")),
                LocalDate.of(2026, 9, 30),
                LocalDateTime.of(2026, 9, 30, 8, 0),
                null,
                null
        ));
    }

    private static SellerCommissionSettlement paid(
            LocalDate periodStart,
            LocalDate periodEnd,
            int orderCount,
            String commission
    ) {
        return SellerCommissionSettlement.createPaid(
                UUID.randomUUID(),
                UUID.randomUUID(),
                periodStart,
                periodEnd,
                FinancialAmount.of(new BigDecimal("1000.00")),
                orderCount,
                FinancialAmount.of(new BigDecimal(commission)),
                periodEnd,
                periodEnd.atStartOfDay(),
                UUID.randomUUID(),
                null
        );
    }
}
