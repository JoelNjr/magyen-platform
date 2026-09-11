package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionAdditionalCostStatusTest {

    @Test
    void createdAndPlannedRejectOtherCost() {
        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );

        ProductionDomainException created = assertThrows(
                ProductionDomainException.class,
                () -> registerOther(productionOrder, LocalDate.of(2026, 9, 2))
        );
        assertEquals(
                "Additional cost can only be registered while status is IN_PROGRESS or COMPLETED. Current status: CREATED",
                created.getMessage()
        );

        productionOrder.plan(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 8), ProductionPriority.NORMAL);
        ProductionDomainException planned = assertThrows(
                ProductionDomainException.class,
                () -> registerOther(productionOrder, LocalDate.of(2026, 9, 3))
        );
        assertEquals(
                "Additional cost can only be registered while status is IN_PROGRESS or COMPLETED. Current status: PLANNED",
                planned.getMessage()
        );
    }

    @Test
    void inProgressAndCompletedAcceptOtherCostWithoutReopeningProduction() {
        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );
        productionOrder.plan(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 8), ProductionPriority.NORMAL);
        productionOrder.start(LocalDate.of(2026, 9, 3));

        ProductionAdditionalCost inProgressCost = registerOther(productionOrder, LocalDate.of(2026, 9, 4));
        assertEquals(ProductionStatus.IN_PROGRESS, productionOrder.getStatus());
        assertEquals(LocalDate.of(2026, 9, 4), inProgressCost.getIncurredDate());

        productionOrder.complete(LocalDate.of(2026, 9, 10));
        LocalDate completionDate = productionOrder.getActualCompletionDate();

        ProductionAdditionalCost completedCost = registerOther(productionOrder, LocalDate.of(2026, 9, 12));

        assertEquals(ProductionStatus.COMPLETED, productionOrder.getStatus());
        assertEquals(completionDate, productionOrder.getActualCompletionDate());
        assertEquals(LocalDate.of(2026, 9, 10), productionOrder.getActualCompletionDate());
        assertEquals(LocalDate.of(2026, 9, 12), completedCost.getIncurredDate());
        assertEquals(2, productionOrder.getAdditionalCosts().size());
    }

    @Test
    void completedStillRejectsMaterialLaborAndOperationChanges() {
        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );
        productionOrder.plan(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 8), ProductionPriority.NORMAL);
        productionOrder.start(LocalDate.of(2026, 9, 3));
        productionOrder.complete(LocalDate.of(2026, 9, 10));

        registerOther(productionOrder, LocalDate.of(2026, 9, 12));
        assertEquals(ProductionStatus.COMPLETED, productionOrder.getStatus());

        assertThrows(ProductionDomainException.class, () -> productionOrder.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.METER,
                "post-complete consume"
        ));
        assertThrows(ProductionDomainException.class, () -> productionOrder.registerLaborWork(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 12),
                "Confección",
                BigDecimal.ONE,
                "UNIT",
                new BigDecimal("10000.00"),
                null
        ));
        assertThrows(ProductionDomainException.class, () -> productionOrder.addOperation(
                ProductionOperationType.CUTTING,
                null,
                null,
                null
        ));
        assertThrows(ProductionDomainException.class, () -> productionOrder.start(LocalDate.of(2026, 9, 13)));
        assertEquals(ProductionStatus.COMPLETED, productionOrder.getStatus());
        assertEquals(LocalDate.of(2026, 9, 10), productionOrder.getActualCompletionDate());
    }

    private static ProductionAdditionalCost registerOther(ProductionOrder productionOrder, LocalDate incurredDate) {
        return productionOrder.registerAdditionalCost(
                ProductionDirectCostCategory.OTHER,
                "Envío de uniformes a Cartagena",
                Money.of(new BigDecimal("80000.00")),
                incurredDate
        );
    }
}
