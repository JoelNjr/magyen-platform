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
        BigDecimal minimumStock,
        String description,
        BigDecimal unitCost,
        String materialType,
        boolean plotterPaperRoll
) {
    public CreateInventoryItemCommand(
            String code,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock
    ) {
        this(code, name, category, unitOfMeasure, stock, minimumStock, null, null, null, false);
    }

    public CreateInventoryItemCommand(
            String code,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            String description
    ) {
        this(code, name, category, unitOfMeasure, stock, minimumStock, description, null, null, false);
    }

    public CreateInventoryItemCommand(
            String code,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock,
            String description,
            BigDecimal unitCost
    ) {
        this(code, name, category, unitOfMeasure, stock, minimumStock, description, unitCost, null, false);
    }
}
