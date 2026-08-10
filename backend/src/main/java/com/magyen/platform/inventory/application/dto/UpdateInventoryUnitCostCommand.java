package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada para configurar el costo unitario actual de un material.
 */
public record UpdateInventoryUnitCostCommand(
        UUID inventoryItemId,
        BigDecimal unitCost
) {
}
