package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Resultado del listado de catálogo de inventario agrupado por código de material.
 */
public record GetInventoryCatalogResult(
        List<InventoryCatalogItemResult> materials
) {
}
