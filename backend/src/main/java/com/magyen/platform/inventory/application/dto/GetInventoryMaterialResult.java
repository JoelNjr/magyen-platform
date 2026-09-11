package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Detalle de un material de catálogo y las unidades físicas que lo componen.
 */
public record GetInventoryMaterialResult(
        InventoryCatalogItemResult material,
        List<GetInventoryItemResult> units
) {
}
