package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryItemCostTest {

    @Test
    void acceptsNullableAndZeroUnitCost() {
        InventoryItem withoutCost = InventoryItem.create(
                MaterialCode.of("COST-NULL"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                null
        );

        InventoryItem zeroCost = InventoryItem.create(
                MaterialCode.of("COST-ZERO"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                BigDecimal.ZERO
        );

        assertNull(withoutCost.getUnitCost());
        assertEquals(new BigDecimal("0.00"), zeroCost.getUnitCost());
    }

    @Test
    void rejectsNegativeUnitCostOnCreateAndUpdate() {
        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("COST-NEG"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("10.0000"),
                null,
                null,
                new BigDecimal("-1.00")
        ));

        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-UPD"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("10.0000"),
                null
        );

        assertThrows(
                InventoryDomainException.class,
                () -> inventoryItem.updateUnitCost(new BigDecimal("-0.01"))
        );
    }

    @Test
    void updateUnitCostDoesNotChangeStock() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-STOCK"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("135.2500"),
                new BigDecimal("30.0000")
        );

        inventoryItem.updateUnitCost(new BigDecimal("15000.00"));

        assertEquals(new BigDecimal("15000.00"), inventoryItem.getUnitCost());
        assertEquals(new BigDecimal("135.2500"), inventoryItem.getStock());
        assertEquals(new BigDecimal("30.0000"), inventoryItem.getMinimumStock());
    }

    @Test
    void outMovementSnapshotsUnitCostAndTotalCost() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-OUT"),
                "Tela deportiva",
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
                InventoryUnitOfMeasure.METER,
                "Production consumption",
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("80.0000"), inventoryItem.getStock());
        assertEquals(new BigDecimal("15000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("300000.00"), movement.getTotalCost());
    }

    @Test
    void movementWithoutConfiguredCostKeepsNullSnapshots() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-NONE"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        );

        InventoryMovement movement = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("5.0000"),
                null,
                null,
                LocalDateTime.now()
        );

        assertNull(movement.getUnitCost());
        assertNull(movement.getTotalCost());
    }

    @Test
    void historicalMovementCostRemainsImmutableWhenItemCostChanges() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-IMM"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        );

        InventoryMovement historical = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("20.0000"),
                null,
                "first",
                LocalDateTime.now()
        );

        inventoryItem.updateUnitCost(new BigDecimal("18000.00"));

        InventoryMovement newer = inventoryItem.registerMovement(
                InventoryMovementType.OUT,
                new BigDecimal("10.0000"),
                null,
                "second",
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("15000.00"), historical.getUnitCost());
        assertEquals(new BigDecimal("300000.00"), historical.getTotalCost());
        assertEquals(new BigDecimal("18000.00"), newer.getUnitCost());
        assertEquals(new BigDecimal("180000.00"), newer.getTotalCost());
        assertEquals(new BigDecimal("18000.00"), inventoryItem.getUnitCost());
    }

    @Test
    void inAndAdjustmentAlsoCaptureConfiguredCost() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("COST-ADJ"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        );

        InventoryMovement inMovement = inventoryItem.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("2.0000"),
                null,
                "receipt",
                LocalDateTime.now()
        );

        InventoryMovement adjustment = inventoryItem.registerMovement(
                InventoryMovementType.ADJUSTMENT,
                new BigDecimal("-1.0000"),
                null,
                "correction",
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("15000.00"), inMovement.getUnitCost());
        assertEquals(new BigDecimal("30000.00"), inMovement.getTotalCost());
        assertEquals(new BigDecimal("15000.00"), adjustment.getUnitCost());
        assertEquals(new BigDecimal("-15000.00"), adjustment.getTotalCost());
    }
}
