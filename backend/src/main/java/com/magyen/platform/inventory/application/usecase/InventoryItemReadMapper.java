package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.domain.InventoryItem;

/**
 * Mapeo compartido del read model de inventario en la capa de aplicación.
 */
final class InventoryItemReadMapper {

    private InventoryItemReadMapper() {
    }

    static GetInventoryItemResult toResult(InventoryItem inventoryItem) {
        return new GetInventoryItemResult(
                inventoryItem.getId(),
                inventoryItem.getMaterialCode().getValue(),
                inventoryItem.getName(),
                inventoryItem.getCategory(),
                inventoryItem.getUnitOfMeasure(),
                inventoryItem.getStock(),
                inventoryItem.getMinimumStock(),
                inventoryItem.getStatus(),
                inventoryItem.getDescription(),
                inventoryItem.isLowStock(),
                inventoryItem.getUnitCost(),
                inventoryItem.getMaterialType(),
                inventoryItem.getPaperRollNumber(),
                inventoryItem.isPlotterPaperRoll()
        );
    }
}
