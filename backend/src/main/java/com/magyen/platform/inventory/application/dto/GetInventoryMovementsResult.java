package com.magyen.platform.inventory.application.dto;

import java.util.List;

/**
 * Resultado del historial de movimientos de un material.
 */
public record GetInventoryMovementsResult(
        List<GetInventoryMovementResult> movements
) {
}
