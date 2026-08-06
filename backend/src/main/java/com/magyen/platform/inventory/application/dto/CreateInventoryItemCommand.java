package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;

/**
 * Entrada del caso de uso para crear un material de inventario.
 */
public record CreateInventoryItemCommand(
        String code,
        String name,
        String category,
        String unitOfMeasure,
        BigDecimal stock,
        BigDecimal minimumStock
) {
}
