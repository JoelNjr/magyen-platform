package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP de un movimiento histórico de inventario.
 */
public record GetInventoryMovementResponse(
        UUID movementId,
        UUID inventoryItemId,
        String movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime movementDate,
        String observation,
        BigDecimal resultingStock,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String sourceType,
        UUID sourceId
) {
}
