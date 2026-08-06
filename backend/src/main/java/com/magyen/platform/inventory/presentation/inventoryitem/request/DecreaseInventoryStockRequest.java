package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para disminuir el stock de un material de inventario.
 */
public record DecreaseInventoryStockRequest(
        BigDecimal quantity
) {
}
