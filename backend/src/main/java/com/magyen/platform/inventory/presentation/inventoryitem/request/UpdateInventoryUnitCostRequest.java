package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para configurar el costo unitario actual.
 */
public record UpdateInventoryUnitCostRequest(
        BigDecimal unitCost
) {
}
