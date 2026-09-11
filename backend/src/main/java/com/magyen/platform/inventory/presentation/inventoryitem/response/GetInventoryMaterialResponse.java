package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.util.List;

/**
 * Detalle HTTP de un material de catálogo y sus unidades físicas.
 */
public record GetInventoryMaterialResponse(
        GetInventoryCatalogItemResponse material,
        List<GetInventoryItemResponse> units
) {
}
