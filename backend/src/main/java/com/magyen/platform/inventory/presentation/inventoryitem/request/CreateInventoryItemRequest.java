package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload HTTP para crear un material de inventario.
 */
public record CreateInventoryItemRequest(
        String code,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock,
        String description,
        BigDecimal unitCost,
        String materialType,
        Boolean plotterPaperRoll,
        InventoryAcquisitionRequest acquisition,
        UUID plotterMachineId
) {
}
