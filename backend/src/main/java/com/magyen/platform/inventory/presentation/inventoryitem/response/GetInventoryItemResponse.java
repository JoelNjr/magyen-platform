package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle de un material de inventario.
 */
public record GetInventoryItemResponse(
        UUID inventoryItemId,
        String materialCode,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock,
        String status
) {
}
