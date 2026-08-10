package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un movimiento de inventario.
 */
public record RegisterInventoryMovementCommand(
        UUID inventoryItemId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        String observation,
        InventoryMovementSourceType sourceType,
        UUID sourceId
) {
    public RegisterInventoryMovementCommand(
            UUID inventoryItemId,
            InventoryMovementType movementType,
            BigDecimal quantity,
            String unitOfMeasure,
            String observation
    ) {
        this(
                inventoryItemId,
                movementType,
                quantity,
                unitOfMeasure,
                observation,
                InventoryMovementSourceType.MANUAL,
                null
        );
    }
}
