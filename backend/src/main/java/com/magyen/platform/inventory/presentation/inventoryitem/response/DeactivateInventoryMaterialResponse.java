package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;

/**
 * Respuesta HTTP tras desactivar un material del inventario activo.
 */
public record DeactivateInventoryMaterialResponse(
        String materialCode,
        String status,
        int deactivatedUnitCount,
        BigDecimal aggregatedStock
) {
}
