package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model de un material de inventario.
 */
public record GetInventoryItemResult(
        UUID inventoryItemId,
        String materialCode,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock,
        InventoryItemStatus status,
        String description,
        boolean lowStock,
        BigDecimal unitCost,
        InventoryMaterialType materialType,
        String paperRollNumber,
        boolean plotterPaperRoll
) {
}
