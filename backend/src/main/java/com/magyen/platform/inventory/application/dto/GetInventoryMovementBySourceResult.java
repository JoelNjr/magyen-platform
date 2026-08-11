package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read model de un movimiento localizado por origen de negocio.
 * <p>
 * {@code unitCost} y {@code totalCost} son el snapshot histórico; pueden ser null
 * cuando el material no tenía costo configurado al momento del movimiento.
 */
public record GetInventoryMovementBySourceResult(
        UUID movementId,
        UUID inventoryItemId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime movementDate,
        String observation,
        BigDecimal resultingStock,
        BigDecimal unitCost,
        BigDecimal totalCost,
        InventoryMovementSourceType sourceType,
        UUID sourceId
) {
}
