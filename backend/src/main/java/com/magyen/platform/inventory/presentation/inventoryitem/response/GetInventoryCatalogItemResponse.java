package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Material de catálogo agrupado por código de negocio.
 */
public record GetInventoryCatalogItemResponse(
        String materialCode,
        String name,
        String category,
        String materialType,
        String unitOfMeasure,
        String description,
        BigDecimal aggregatedStock,
        BigDecimal minimumStock,
        boolean minimumStockUniform,
        boolean lowStock,
        String status,
        BigDecimal unitCost,
        boolean unitCostUniform,
        int physicalUnitCount,
        boolean paperMaterial,
        UUID stockHoldingItemId
) {
}
