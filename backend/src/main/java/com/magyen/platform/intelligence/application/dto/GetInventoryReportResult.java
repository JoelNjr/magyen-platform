package com.magyen.platform.intelligence.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del caso de uso de reporte de inventario con stock bajo el mínimo.
 */
public record GetInventoryReportResult(
        List<LowStockItem> items
) {

    /**
     * Material cuyo stock disponible se encuentra por debajo del stock mínimo.
     */
    public record LowStockItem(
            UUID inventoryItemId,
            String materialCode,
            String name,
            String category,
            String unitOfMeasure,
            BigDecimal stock,
            BigDecimal minimumStock
    ) {
    }
}
