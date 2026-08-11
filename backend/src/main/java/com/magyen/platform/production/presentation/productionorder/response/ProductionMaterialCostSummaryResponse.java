package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;

/**
 * Resumen HTTP de atribución de costo de materiales de producción.
 */
public record ProductionMaterialCostSummaryResponse(
        BigDecimal totalMaterialCost,
        int consumptionCount,
        int valuedConsumptionCount,
        int unvaluedConsumptionCount
) {
}
