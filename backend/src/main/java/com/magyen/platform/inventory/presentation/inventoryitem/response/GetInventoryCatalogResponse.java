package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de catálogo de inventario.
 */
public record GetInventoryCatalogResponse(
        List<GetInventoryCatalogItemResponse> materials
) {
}
