package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada para consumir material de inventario con origen auditable (OUT).
 */
public record ConsumeInventoryMaterialCommand(
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        InventoryMovementSourceType sourceType,
        UUID sourceId,
        String observation
) {
}
