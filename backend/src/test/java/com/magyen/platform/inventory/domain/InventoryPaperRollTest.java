package com.magyen.platform.inventory.domain;

import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryPaperRollTest {

    @Test
    void createsPlotterPaperRollWithRpNumber() {
        InventoryItem roll = InventoryItem.create(
                MaterialCode.of("PAPER-001"),
                "Sublimación 1.60m",
                "PAPER",
                "METER",
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                "Rollo plotter",
                new BigDecimal("4500.00"),
                InventoryMaterialType.PAPER,
                "RP-001"
        );

        assertTrue(roll.isPlotterPaperRoll());
        assertEquals("RP-001", roll.getPaperRollNumber());
        assertEquals(InventoryMaterialType.PAPER, roll.getMaterialType());
        assertEquals("METER", roll.getUnitOfMeasure());
    }

    @Test
    void historicalMaterialsRemainValidWithoutPaperRollNumber() {
        InventoryItem fabric = InventoryItem.create(
                MaterialCode.of("TELA-001"),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("10.0000"),
                null
        );

        assertEquals(InventoryMaterialType.OTHER, fabric.getMaterialType());
        assertNull(fabric.getPaperRollNumber());
        assertFalse(fabric.isPlotterPaperRoll());
    }

    @Test
    void rejectsPaperRollNumberOnNonPaperMaterial() {
        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("INK-001"),
                "Tinta",
                "INK",
                "LITER",
                new BigDecimal("5.0000"),
                null,
                null,
                null,
                InventoryMaterialType.INK,
                "RP-099"
        ));
    }

    @Test
    void rejectsPaperRollWithNonMeterUnit() {
        assertThrows(InventoryDomainException.class, () -> InventoryItem.create(
                MaterialCode.of("PAPER-002"),
                "Papel",
                "PAPER",
                "ROLL",
                new BigDecimal("1.0000"),
                null,
                null,
                null,
                InventoryMaterialType.PAPER,
                "RP-002"
        ));
    }
}
