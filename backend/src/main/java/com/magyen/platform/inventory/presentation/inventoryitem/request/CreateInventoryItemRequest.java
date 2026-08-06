package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para crear un material de inventario.
 */
public record CreateInventoryItemRequest(
        String code,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock
) {
}
