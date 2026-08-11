package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;

/**
 * Resumen HTTP de atribución de costo de mano de obra de producción.
 */
public record ProductionLaborCostSummaryResponse(
        BigDecimal totalLaborCost,
        int laborWorkCount,
        int pendingCount,
        int paidCount
) {
}
