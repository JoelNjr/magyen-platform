package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryItemMovementTest {

    @Test
    void createsValidInventoryItemWithCanonicalUnitAndOptionalDescription() {
        InventoryItem inventoryItem = InventoryItem.create(
                MaterialCode.of("TELA-100"),
                "Tela Hydrotech",
                "FABRIC",
                "METRO",
                new BigDecimal("10.0000"),
                new BigDecimal("2.0000"),
                "Tela Lafayette Hydrotech"
        );

        assertNotNull(inventoryItem.getId());
        assertEquals("METER", inventoryItem.getUnitOfMeasure());
        assertTrue(inventoryItem.getUnitOfMeasureValue().isCanonical());
        assertEquals("Tela Lafayette Hydrotech", inventoryItem.getDescription());
        assertEquals(InventoryItemStatus.ACTIVE, inventoryItem.getStatus());
    }

    @Test
    void rejectsBlankNameAndNegativeStocks() {
        assertThrows(IllegalArgumentException.class, () -> InventoryItem.create(
                MaterialCode.of("TELA-101"),
                " ",
                "FABRIC",
                "METER",
                BigDecimal.ONE,
                BigDecimal.ZERO
        ));

        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("TELA-102"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("-1"),
                BigDecimal.ZERO
        ));

        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("TELA-103"),
                "Tela",
                "FABRIC",
                "METER",
                BigDecimal.ZERO,
                new BigDecimal("-1")
        ));
    }

    @Test
    void rejectsUnsupportedUnitOnCreateButPreservesUnknownLegacyOnReconstitute() {
        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("TELA-104"),
                "Tela",
                "FABRIC",
                "YARD",
                BigDecimal.ONE,
                BigDecimal.ZERO
        ));

        InventoryItem reconstituted = InventoryItem.reconstitute(
                java.util.UUID.randomUUID(),
                MaterialCode.of("LEGACY-1"),
                "Legacy material",
                "OTHER",
                "YARD",
                new BigDecimal("5.0000"),
                BigDecimal.ONE,
                InventoryItemStatus.ACTIVE,
                "legacy description"
        );

        assertEquals("YARD", reconstituted.getUnitOfMeasure());
        assertTrue(!reconstituted.getUnitOfMeasureValue().isCanonical());
    }

    @Test
    void inIncreasesStockAndOutDecreasesWithoutGoingNegative() {
        InventoryItem inventoryItem = createFabric(new BigDecimal("10.0000"));

        InventoryMovement inMovement = inventoryItem.registerMovement(
                InventoryMovementType.IN,
                new BigDecimal("5.0000"),
                InventoryUnitOfMeasure.METER,
                "Purchase",
                LocalDateTime.now()
        );
        assertEquals(new BigDecimal("15.0000"), inventoryItem.getStock());
        assertEquals(new BigDecimal("15.0000"), inMovement.getResultingStock());
        assertNotNull(inMovement.getId());

        InventoryMovement outMovement = inventoryItem.decreaseStock(new BigDecimal("4.0000"));
        assertEquals(new BigDecimal("11.0000"), inventoryItem.getStock());
        assertEquals(InventoryMovementType.OUT, outMovement.getMovementType());

        assertThrows(InventoryDomainException.class, () -> inventoryItem.decreaseStock(new BigDecimal("20.0000")));
        assertEquals(new BigDecimal("11.0000"), inventoryItem.getStock());
    }

    @Test
    void rejectsZeroAndNegativeQuantitiesForInAndOut() {
        InventoryItem inventoryItem = createFabric(new BigDecimal("10.0000"));

        assertThrows(InventoryDomainException.class, () -> inventoryItem.increaseStock(BigDecimal.ZERO));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.increaseStock(new BigDecimal("-1")));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.decreaseStock(BigDecimal.ZERO));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.decreaseStock(new BigDecimal("-1")));
    }

    @Test
    void adjustmentAppliesSignedDeltaDeterministically() {
        InventoryItem inventoryItem = createFabric(new BigDecimal("10.0000"));

        InventoryMovement positiveAdjustment = inventoryItem.registerMovement(
                InventoryMovementType.ADJUSTMENT,
                new BigDecimal("2.5000"),
                null,
                "Cycle count surplus",
                LocalDateTime.now()
        );
        assertEquals(new BigDecimal("12.5000"), inventoryItem.getStock());
        assertEquals(new BigDecimal("12.5000"), positiveAdjustment.getResultingStock());

        InventoryMovement negativeAdjustment = inventoryItem.registerMovement(
                InventoryMovementType.ADJUSTMENT,
                new BigDecimal("-1.5000"),
                InventoryUnitOfMeasure.METER,
                "Cycle count shortage",
                LocalDateTime.now()
        );
        assertEquals(new BigDecimal("11.0000"), inventoryItem.getStock());
        assertEquals(InventoryMovementType.ADJUSTMENT, negativeAdjustment.getMovementType());

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.ADJUSTMENT,
                BigDecimal.ZERO,
                null,
                "invalid",
                LocalDateTime.now()
        ));

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.ADJUSTMENT,
                new BigDecimal("-20.0000"),
                null,
                "too large",
                LocalDateTime.now()
        ));
        assertEquals(new BigDecimal("11.0000"), inventoryItem.getStock());
    }

    @Test
    void rejectsIncompatibleUnitAndPreservesMovementIdentity() {
        InventoryItem inventoryItem = createFabric(new BigDecimal("10.0000"));

        assertThrows(InventoryDomainException.class, () -> inventoryItem.registerMovement(
                InventoryMovementType.IN,
                BigDecimal.ONE,
                InventoryUnitOfMeasure.KILOGRAM,
                "wrong unit",
                LocalDateTime.now()
        ));

        InventoryMovement first = inventoryItem.increaseStock(BigDecimal.ONE);
        InventoryMovement second = inventoryItem.increaseStock(BigDecimal.ONE);

        assertNotEquals(first.getId(), second.getId());
        assertEquals(inventoryItem.getId(), first.getInventoryItemId());
        assertEquals(inventoryItem.getId(), second.getInventoryItemId());
    }

    @Test
    void activateAndDeactivateToggleStatus() {
        InventoryItem inventoryItem = createFabric(BigDecimal.TEN);

        inventoryItem.deactivate();
        assertEquals(InventoryItemStatus.INACTIVE, inventoryItem.getStatus());

        inventoryItem.activate();
        assertEquals(InventoryItemStatus.ACTIVE, inventoryItem.getStatus());
    }

    private InventoryItem createFabric(BigDecimal stock) {
        return InventoryItem.create(
                MaterialCode.of("TELA-" + java.util.UUID.randomUUID().toString().substring(0, 8)),
                "Tela",
                "FABRIC",
                "METER",
                stock,
                new BigDecimal("1.0000")
        );
    }
}
