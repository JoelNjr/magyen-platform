package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del caso de uso de incremento de stock de inventario.
 */
public record IncreaseInventoryStockResult(
        UUID inventoryItemId,
        BigDecimal stock,
        InventoryItemStatus status
) {
}
