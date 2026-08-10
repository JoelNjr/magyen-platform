package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload HTTP para registrar un movimiento de inventario.
 * <p>
 * {@code sourceType} es opcional: si se omite, se interpreta como {@code MANUAL}.
 */
public record RegisterInventoryMovementRequest(
        String movementType,
        BigDecimal quantity,
        String unitOfMeasure,
        String observation,
        String sourceType,
        UUID sourceId
) {
}
