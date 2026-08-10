package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del registro de un movimiento de inventario.
 */
public record RegisterInventoryMovementResult(
        UUID movementId,
        UUID inventoryItemId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal resultingStock,
        LocalDateTime movementDate,
        String observation,
        BigDecimal unitCost,
        BigDecimal totalCost,
        InventoryMovementSourceType sourceType,
        UUID sourceId
) {
}
