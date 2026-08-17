package com.magyen.platform.production.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionOperatorTest {

    @Test
    void createKeepsStableIdAndTrimsName() {
        ProductionOperator operator = ProductionOperator.create("  Jean Carlos  ");

        assertEquals("Jean Carlos", operator.getName());
        assertTrue(operator.isActive());

        UUID originalId = operator.getId();
        operator.rename("Jean Carlos R.");
        operator.deactivate();

        assertEquals(originalId, operator.getId());
        assertEquals("Jean Carlos R.", operator.getName());
        assertFalse(operator.isActive());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> ProductionOperator.create("   "));
        assertThrows(NullPointerException.class, () -> ProductionOperator.create(null));
    }
}
