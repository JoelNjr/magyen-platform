package com.magyen.platform.inventory.application.dto;

/**
 * Filtros opcionales para listar materiales de inventario.
 */
public record GetInventoryItemsQuery(
        String materialType,
        Boolean plotterPaperRoll
) {
    public static GetInventoryItemsQuery all() {
        return new GetInventoryItemsQuery(null, null);
    }
}
