package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryMovementSourceTest {

    @Test
    void manualSourceAllowsNullSourceId() {
        InventoryItem inventoryItem = createFabric();

        InventoryMovement movement = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("5.0000"),
                null,
                "manual out",
                LocalDateTime.now(),
                InventoryMovementSourceType.MANUAL,
                null
        );

        assertEquals(InventoryMovementSourceType.MANUAL, movement.getSourceType());
        assertNull(movement.getSourceId());
    }

    @Test
    void productionAndPlotterRequireSourceId() {
        InventoryItem inventoryItem = createFabric();
        UUID productionOrderId = UUID.randomUUID();
        UUID plotterJobId = UUID.randomUUID();

        InventoryMovement productionMovement = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("2.0000"),
                null,
                "production",
                LocalDateTime.now(),
                InventoryMovementSourceType.PRODUCTION,
                productionOrderId
        );

        InventoryMovement plotterMovement = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("1.0000"),
                null,
                "plotter",
                LocalDateTime.now(),
                InventoryMovementSourceType.PLOTTER,
                plotterJobId
        );

        assertEquals(InventoryMovementSourceType.PRODUCTION, productionMovement.getSourceType());
        assertEquals(productionOrderId, productionMovement.getSourceId());
        assertEquals(InventoryMovementSourceType.PLOTTER, plotterMovement.getSourceType());
        assertEquals(plotterJobId, plotterMovement.getSourceId());
    }

    @Test
    void rejectsProductionOrPlotterWithoutSourceId() {
        InventoryItem inventoryItem = createFabric();

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDateTime.now(),
                InventoryMovementSourceType.PRODUCTION,
                null
        ));

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDateTime.now(),
                InventoryMovementSourceType.PLOTTER,
                null
        ));
    }

    @Test
    void rejectsInvalidSourceType() {
        assertThrows(InventoryDomainException.class, () -> InventoryMovementSourceType.of("BUY"));
    }

    @Test
    void purchaseRequiresSourceIdAndSnapshotsPurchaseUnitCost() {
        InventoryItem inventoryItem = createFabric();
        UUID purchaseId = UUID.randomUUID();

        InventoryMovement movement = inventoryItem.registerPurchase(
                new BigDecimal("100.0000"),
                new BigDecimal("10000.00"),
                "compra",
                LocalDateTime.now(),
                purchaseId
        );

        assertEquals(InventoryMovementSourceType.PURCHASE, movement.getSourceType());
        assertEquals(purchaseId, movement.getSourceId());
        assertEquals(new BigDecimal("10000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("1000000.00"), movement.getTotalCost());
        assertEquals(new BigDecimal("150.0000"), inventoryItem.getStock());
        assertEquals(new BigDecimal("10000.00"), inventoryItem.getUnitCost());

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerPurchase(
                BigDecimal.ZERO,
                new BigDecimal("10000.00"),
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        ));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerPurchase(
                new BigDecimal("-1.0000"),
                new BigDecimal("10000.00"),
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        ));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerPurchase(
                new BigDecimal("1.0000"),
                BigDecimal.ZERO,
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        ));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerPurchase(
                new BigDecimal("1.0000"),
                new BigDecimal("-1.00"),
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        ));

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                null,
                null,
                LocalDateTime.now(),
                InventoryMovementSourceType.PURCHASE,
                null
        ));
    }

    @Test
    void reconstitutesNullSourceTypeAsManual() {
        InventoryMovement movement = InventoryMovement.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                InventoryUnitOfMeasure.METER,
                LocalDateTime.now(),
                null,
                new BigDecimal("10.0000"),
                null,
                null,
                null,
                null
        );

        assertEquals(InventoryMovementSourceType.MANUAL, movement.getSourceType());
        assertNull(movement.getSourceId());
    }

    @Test
    void sourceDoesNotAlterCostSnapshot() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("SRC-COST"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        );

        InventoryMovement movement = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("20.0000"),
                null,
                "costed",
                LocalDateTime.now(),
                InventoryMovementSourceType.MANUAL,
                null
        );

        assertEquals(new BigDecimal("15000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("300000.00"), movement.getTotalCost());
        assertEquals(InventoryMovementSourceType.MANUAL, movement.getSourceType());
    }

    private static InventoryItem createFabric() {
        return InventoryItem.create(
                MaterialCode.of("SRC-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        );
    }
}
