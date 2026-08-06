package com.magyen.platform.inventory.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para consultar un material de inventario.
 */
public record GetInventoryItemQuery(
        UUID inventoryItemId
) {
}
