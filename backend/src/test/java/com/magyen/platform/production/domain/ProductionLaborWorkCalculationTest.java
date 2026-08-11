package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionLaborWorkAlreadyPaidException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionLaborWorkCalculationTest {

    @Test
    void calculatesAmountAsQuantityTimesUnitRateWithHalfUpScaleTwo() {
        ProductionLaborWork laborWork = ProductionLaborWork.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal("100"),
                "UNIT",
                new BigDecimal("800"),
                null
        );

        assertEquals(new BigDecimal("80000.00"), laborWork.getCalculatedAmount());
        assertEquals(ProductionLaborWorkStatus.PENDING, laborWork.getStatus());
        assertNull(laborWork.getPaidAt());
        assertNull(laborWork.getFinancialTransactionId());
    }

    @Test
    void roundsMonetaryProductHalfUp() {
        ProductionLaborWork laborWork = ProductionLaborWork.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                "Acabado",
                new BigDecimal("3.3333"),
                "UNIT",
                new BigDecimal("1.50"),
                null
        );

        assertEquals(new BigDecimal("5.00"), laborWork.getCalculatedAmount());
    }

    @Test
    void rejectsInvalidQuantityUnitRateAndZeroProduct() {
        UUID productionOrderId = UUID.randomUUID();
        UUID operatorEmployeeId = UUID.randomUUID();
        LocalDate workDate = LocalDate.of(2026, 8, 10);

        assertThrows(ProductionDomainException.class, () -> ProductionLaborWork.create(
                productionOrderId,
                operatorEmployeeId,
                workDate,
                "Confección",
                BigDecimal.ZERO,
                "UNIT",
                new BigDecimal("800"),
                null
        ));

        assertThrows(ProductionDomainException.class, () -> ProductionLaborWork.create(
                productionOrderId,
                operatorEmployeeId,
                workDate,
                "Confección",
                new BigDecimal("10"),
                "UNIT",
                new BigDecimal("-1.00"),
                null
        ));

        assertThrows(ProductionDomainException.class, () -> ProductionLaborWork.create(
                productionOrderId,
                operatorEmployeeId,
                workDate,
                "Confección",
                new BigDecimal("10"),
                "UNIT",
                BigDecimal.ZERO,
                null
        ));
    }

    @Test
    void markPaidAndCancelEnforceTerminalTransitions() {
        ProductionLaborWork laborWork = ProductionLaborWork.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal("10"),
                "UNIT",
                new BigDecimal("500"),
                null
        );

        UUID transactionId = UUID.randomUUID();
        laborWork.markPaid(transactionId, LocalDateTime.of(2026, 8, 11, 10, 0));
        assertEquals(ProductionLaborWorkStatus.PAID, laborWork.getStatus());
        assertEquals(transactionId, laborWork.getFinancialTransactionId());

        assertThrows(ProductionLaborWorkAlreadyPaidException.class, () -> laborWork.markPaid(
                UUID.randomUUID(),
                LocalDateTime.now()
        ));
        assertThrows(ProductionDomainException.class, laborWork::cancel);

        ProductionLaborWork cancellable = ProductionLaborWork.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                "Corte",
                new BigDecimal("5"),
                "UNIT",
                new BigDecimal("200"),
                null
        );
        cancellable.cancel();
        assertEquals(ProductionLaborWorkStatus.CANCELLED, cancellable.getStatus());
        assertThrows(ProductionDomainException.class, () -> cancellable.markPaid(
                UUID.randomUUID(),
                LocalDateTime.now()
        ));
    }

    @Test
    void historicalRateIsFrozenOnCreate() {
        ProductionLaborWork laborWork = ProductionLaborWork.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal("100"),
                "UNIT",
                new BigDecimal("800.00"),
                null
        );

        assertEquals(new BigDecimal("800.00"), laborWork.getUnitRate());
        assertEquals(new BigDecimal("80000.00"), laborWork.getCalculatedAmount());
    }
}
