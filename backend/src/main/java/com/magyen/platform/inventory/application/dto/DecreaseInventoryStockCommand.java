package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para disminuir el stock de un material de inventario.
 */
public record DecreaseInventoryStockCommand(
        UUID inventoryItemId,
        BigDecimal quantity
) {
}
