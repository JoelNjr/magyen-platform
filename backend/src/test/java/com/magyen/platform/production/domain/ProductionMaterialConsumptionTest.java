package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionMaterialConsumptionTest {

    @Test
    void registersValidConsumptionForInProgressOrder() {
        ProductionOrder productionOrder = inProgressOrder();
        UUID inventoryItemId = UUID.randomUUID();

        ProductionMaterialConsumption consumption = productionOrder.registerMaterialConsumption(
                inventoryItemId,
                new BigDecimal("18.7000"),
                ProductionMaterialUnitOfMeasure.METER,
                "Tela utilizada durante confección"
        );

        assertNotNull(consumption.getId());
        assertEquals(productionOrder.getId(), consumption.getProductionOrderId());
        assertEquals(inventoryItemId, consumption.getInventoryItemId());
        assertEquals(new BigDecimal("18.7000"), consumption.getQuantity());
        assertEquals(ProductionMaterialUnitOfMeasure.METER, consumption.getUnitOfMeasure());
        assertNotNull(consumption.getConsumptionDate());
        assertEquals("Tela utilizada durante confección", consumption.getObservation());
        assertEquals(1, productionOrder.getMaterialConsumptions().size());
    }

    @Test
    void rejectsNonPositiveQuantityAndNullReferences() {
        ProductionOrder productionOrder = inProgressOrder();

        assertThrows(ProductionDomainException.class, () -> productionOrder.registerMaterialConsumption(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                ProductionMaterialUnitOfMeasure.METER,
                null
        ));

        assertThrows(ProductionDomainException.class, () -> productionOrder.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("-1.0000"),
                ProductionMaterialUnitOfMeasure.METER,
                null
        ));

        assertThrows(NullPointerException.class, () -> productionOrder.registerMaterialConsumption(
                null,
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.METER,
                null
        ));

        assertThrows(NullPointerException.class, () -> ProductionMaterialConsumption.reconstitute(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.METER,
                java.time.LocalDateTime.now(),
                null
        ));
    }

    @Test
    void rejectsInvalidUnitAndNormalizesBlankObservation() {
        assertThrows(ProductionDomainException.class, () -> ProductionMaterialUnitOfMeasure.of("YARD"));

        ProductionOrder productionOrder = inProgressOrder();
        ProductionMaterialConsumption consumption = productionOrder.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.of("METRO"),
                "   "
        );

        assertEquals(ProductionMaterialUnitOfMeasure.METER, consumption.getUnitOfMeasure());
        assertNull(consumption.getObservation());
    }

    @Test
    void rejectsConsumptionOutsideInProgress() {
        ProductionOrder created = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );

        assertThrows(ProductionDomainException.class, () -> created.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.UNIT,
                null
        ));

        created.plan(LocalDate.now(), LocalDate.now().plusDays(1), ProductionPriority.HIGH);
        assertThrows(ProductionDomainException.class, () -> created.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.UNIT,
                null
        ));

        created.start();
        created.complete();
        assertThrows(ProductionDomainException.class, () -> created.registerMaterialConsumption(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                ProductionMaterialUnitOfMeasure.UNIT,
                null
        ));
    }

    private static ProductionOrder inProgressOrder() {
        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );
        productionOrder.plan(LocalDate.now(), LocalDate.now().plusDays(2), ProductionPriority.NORMAL);
        productionOrder.start();
        return productionOrder;
    }
}
