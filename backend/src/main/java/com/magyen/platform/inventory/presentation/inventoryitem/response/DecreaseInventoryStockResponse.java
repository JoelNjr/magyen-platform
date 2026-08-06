package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP tras disminuir el stock de un material de inventario.
 */
public record DecreaseInventoryStockResponse(
        UUID inventoryItemId,
        BigDecimal stock,
        String status
) {
}
