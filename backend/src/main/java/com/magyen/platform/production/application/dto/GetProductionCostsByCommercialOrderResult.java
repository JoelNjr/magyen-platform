package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot de costos de producción para una Orden comercial.
 * <p>
 * {@code materialCost} y {@code laborCost} son {@link BigDecimal#ZERO} cuando no hay
 * valorización o registros aplicables (no se inventa null para el agregador comercial).
 */
public record GetProductionCostsByCommercialOrderResult(
        UUID productionOrderId,
        boolean productionOrderFound,
        BigDecimal materialCost,
        int materialConsumptionCount,
        int valuedMaterialConsumptionCount,
        int unvaluedMaterialConsumptionCount,
        BigDecimal laborCost,
        int laborWorkCount,
        int pendingLaborCount,
        int paidLaborCount
) {
}
