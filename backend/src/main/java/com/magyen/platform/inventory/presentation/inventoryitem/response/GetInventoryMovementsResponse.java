package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.util.List;

/**
 * Respuesta HTTP del historial de movimientos de un material.
 */
public record GetInventoryMovementsResponse(
        List<GetInventoryMovementResponse> movements
) {
}
