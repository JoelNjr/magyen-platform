package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Resultado del listado de materiales de inventario.
 */
public record GetInventoryItemsResult(
        List<GetInventoryItemResult> items
) {
}
