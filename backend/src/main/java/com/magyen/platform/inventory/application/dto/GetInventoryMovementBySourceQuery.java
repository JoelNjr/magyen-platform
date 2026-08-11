package com.magyen.platform.inventory.application.dto;

import com.magyen.platform.inventory.domain.InventoryMovementSourceType;

import java.util.UUID;

/**
 * Consulta un movimiento de inventario por su origen de negocio.
 */
public record GetInventoryMovementBySourceQuery(
        InventoryMovementSourceType sourceType,
        UUID sourceId
) {
}
