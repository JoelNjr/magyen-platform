package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP tras crear un material de inventario exitosamente.
 */
public record CreateInventoryItemResponse(
        UUID inventoryItemId,
        String materialCode,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock,
        String status,
        String description,
        boolean lowStock,
        BigDecimal unitCost,
        String materialType,
        String paperRollNumber,
        boolean plotterPaperRoll
) {
}
