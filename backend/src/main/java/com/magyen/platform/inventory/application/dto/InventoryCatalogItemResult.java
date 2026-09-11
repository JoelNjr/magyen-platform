package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model de un material de catálogo agrupado por {@code materialCode}.
 * <p>
 * {@code unitCost} solo se informa cuando todas las unidades tienen el mismo costo.
 * El stock agregado es derivado; no se persiste.
 */
public record InventoryCatalogItemResult(
        String materialCode,
        String name,
        String category,
        InventoryMaterialType materialType,
        String unitOfMeasure,
        String description,
        BigDecimal aggregatedStock,
        BigDecimal minimumStock,
        boolean minimumStockUniform,
        boolean lowStock,
        InventoryItemStatus status,
        BigDecimal unitCost,
        boolean unitCostUniform,
        int physicalUnitCount,
        boolean paperMaterial,
        UUID stockHoldingItemId
) {
}
