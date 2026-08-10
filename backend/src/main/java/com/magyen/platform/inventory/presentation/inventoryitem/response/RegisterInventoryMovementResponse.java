package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP tras registrar un movimiento de inventario.
 */
public record RegisterInventoryMovementResponse(
        UUID movementId,
        UUID inventoryItemId,
        String movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal resultingStock,
        LocalDateTime movementDate,
        String observation,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String sourceType,
        UUID sourceId
) {
}
