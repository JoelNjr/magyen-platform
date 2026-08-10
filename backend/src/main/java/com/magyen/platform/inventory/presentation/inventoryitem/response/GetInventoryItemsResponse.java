package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de materiales de inventario.
 */
public record GetInventoryItemsResponse(
        List<GetInventoryItemResponse> items
) {
}
