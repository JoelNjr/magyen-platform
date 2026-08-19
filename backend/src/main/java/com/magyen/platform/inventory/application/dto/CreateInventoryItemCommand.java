package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;

/**
 * Entrada del caso de uso para crear un material de inventario.
 * <p>
 * {@code code} se ignora: el código lo genera la aplicación.
 * {@code acquisition} registra la compra inicial (tela, papel u otro material).
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
        boolean plotterPaperRoll,
        InventoryAcquisitionCommand acquisition
) {
    public CreateInventoryItemCommand(
            String code,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock
    ) {
        this(code, name, category, unitOfMeasure, stock, minimumStock, null, null, null, false, null);
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
        this(code, name, category, unitOfMeasure, stock, minimumStock, description, null, null, false, null);
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
        this(code, name, category, unitOfMeasure, stock, minimumStock, description, unitCost, null, false, null);
    }

    public CreateInventoryItemCommand(
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
        this(
                code,
                name,
                category,
                unitOfMeasure,
                stock,
                minimumStock,
                description,
                unitCost,
                materialType,
                plotterPaperRoll,
                null
        );
    }
}
