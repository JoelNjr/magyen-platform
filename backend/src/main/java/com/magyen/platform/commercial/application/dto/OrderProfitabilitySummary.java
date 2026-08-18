package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;

/**
 * Totales de rentabilidad directa. Los montos solo agregan estados COMPLETE
 * para no tratar costos desconocidos como cero. El margen es ponderado:
 * ganancia directa total / valor total * 100.
 */
public record OrderProfitabilitySummary(
        int evaluatedOrderCount,
        int completeOrderCount,
        int partiallyUnvaluedOrderCount,
        int noCostDataOrderCount,
        BigDecimal totalOrderValue,
        BigDecimal totalDirectCost,
        BigDecimal totalDirectProfit,
        BigDecimal weightedMarginPercentage,
        int unvaluedCostCount
) {
}
