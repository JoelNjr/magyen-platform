package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de un consumo de material en inventario.
 */
public record ConsumeInventoryMaterialResult(
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
        UUID sourceId,
        boolean alreadyProcessed
) {
}
