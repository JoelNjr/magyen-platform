package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para incrementar el stock de un material de inventario.
 */
public record IncreaseInventoryStockRequest(
        BigDecimal quantity
) {
}
