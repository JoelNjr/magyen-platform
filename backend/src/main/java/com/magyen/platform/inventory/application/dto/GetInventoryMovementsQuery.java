package com.magyen.platform.inventory.application.dto;

import java.util.UUID;

/**
 * Consulta del historial de movimientos de un material.
 */
public record GetInventoryMovementsQuery(
        UUID inventoryItemId
) {
}
