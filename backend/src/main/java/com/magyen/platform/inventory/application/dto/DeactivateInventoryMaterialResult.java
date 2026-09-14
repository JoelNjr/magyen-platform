package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryItemStatus;

import java.math.BigDecimal;

/**
 * Resultado de desactivar un material de inventario.
 */
public record DeactivateInventoryMaterialResult(
        String materialCode,
        InventoryItemStatus status,
        int deactivatedUnitCount,
        BigDecimal aggregatedStock
) {
}
