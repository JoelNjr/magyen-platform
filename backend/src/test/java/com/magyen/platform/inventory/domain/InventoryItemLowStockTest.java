package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryItemLowStockTest {

    @Test
    void acceptsValidMinimumStockIncludingZeroAndNull() {
        InventoryItem withThreshold = createItem(new BigDecimal("10.0000"), new BigDecimal("20.0000"));
        assertEquals(new BigDecimal("20.0000"), withThreshold.getMinimumStock());

        InventoryItem withZero = createItem(new BigDecimal("10.0000"), BigDecimal.ZERO);
        assertEquals(0, withZero.getMinimumStock().compareTo(BigDecimal.ZERO));

        InventoryItem withoutThreshold = createItem(new BigDecimal("10.0000"), null);
        assertNull(withoutThreshold.getMinimumStock());
        assertFalse(withoutThreshold.isLowStock());
    }

    @Test
    void rejectsNegativeMinimumStockOnCreateAndUpdate() {
        assertThrows(InventoryDomainException.class, () -> createItem(BigDecimal.TEN, new BigDecimal("-1")));

        InventoryItem inventoryItem = createItem(BigDecimal.TEN, new BigDecimal("5.0000"));
        assertThrows(InventoryDomainException.class, () -> inventoryItem.updateMinimumStock(new BigDecimal("-0.01")));
        assertEquals(new BigDecimal("5.0000"), inventoryItem.getMinimumStock());
    }

    @Test
    void calculatesLowStockDeterministically() {
        InventoryItem below = createItem(new BigDecimal("19.9999"), new BigDecimal("20.0000"));
        assertTrue(below.isLowStock());

        InventoryItem equal = createItem(new BigDecimal("20.0000"), new BigDecimal("20.0000"));
        assertTrue(equal.isLowStock());

        InventoryItem above = createItem(new BigDecimal("20.0001"), new BigDecimal("20.0000"));
        assertFalse(above.isLowStock());
    }

    @Test
    void updateMinimumStockDoesNotChangeCurrentStock() {
        InventoryItem inventoryItem = createItem(new BigDecimal("12.5000"), new BigDecimal("10.0000"));

        inventoryItem.updateMinimumStock(new BigDecimal("30.0000"));

        assertEquals(new BigDecimal("12.5000"), inventoryItem.getStock());
        assertEquals(new BigDecimal("30.0000"), inventoryItem.getMinimumStock());
        assertTrue(inventoryItem.isLowStock());

        inventoryItem.updateMinimumStock(null);
        assertNull(inventoryItem.getMinimumStock());
        assertFalse(inventoryItem.isLowStock());
        assertEquals(new BigDecimal("12.5000"), inventoryItem.getStock());
    }

    private InventoryItem createItem(BigDecimal stock, BigDecimal minimumStock) {
        return InventoryItem.create(
                MaterialCode.of("LOW-" + java.util.UUID.randomUUID().toString().substring(0, 8)),
                "Material",
                "CATEGORY",
                "METER",
                stock,
                minimumStock,
                "description"
        );
    }
}
