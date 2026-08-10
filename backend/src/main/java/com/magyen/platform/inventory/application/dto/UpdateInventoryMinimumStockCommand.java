package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada para configurar el umbral mínimo de stock de un material.
 */
public record UpdateInventoryMinimumStockCommand(
        UUID inventoryItemId,
        BigDecimal minimumStock
) {
}
