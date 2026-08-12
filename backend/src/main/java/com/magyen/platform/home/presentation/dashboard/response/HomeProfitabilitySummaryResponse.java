package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;

/**
 * Sección HTTP de resumen de rentabilidad directa.
 */
public record HomeProfitabilitySummaryResponse(
        int evaluatedOrderCount,
        int completeOrderCount,
        int partiallyUnvaluedOrderCount,
        int noCostDataOrderCount,
        BigDecimal totalOrderValue,
        BigDecimal totalDirectCost,
        BigDecimal totalDirectProfit,
        BigDecimal averageMarginPercentage,
        int unvaluedCostCount
) {
}
