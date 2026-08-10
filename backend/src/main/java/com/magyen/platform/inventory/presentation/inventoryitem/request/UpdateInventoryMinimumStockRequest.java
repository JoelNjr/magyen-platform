package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para configurar el umbral mínimo de stock.
 */
public record UpdateInventoryMinimumStockRequest(
        BigDecimal minimumStock
) {
}
