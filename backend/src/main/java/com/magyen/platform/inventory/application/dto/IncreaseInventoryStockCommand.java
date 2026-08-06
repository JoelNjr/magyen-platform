package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para incrementar el stock de un material de inventario.
 */
public record IncreaseInventoryStockCommand(
        UUID inventoryItemId,
        BigDecimal quantity
) {
}
