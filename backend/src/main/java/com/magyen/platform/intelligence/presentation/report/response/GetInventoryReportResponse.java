package com.magyen.platform.intelligence.presentation.report.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP del reporte de inventario con stock bajo el mínimo.
 */
public record GetInventoryReportResponse(
        List<LowStockItemResponse> items
) {

    /**
     * Material con stock por debajo del mínimo.
     */
    public record LowStockItemResponse(
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
