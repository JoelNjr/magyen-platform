package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;

/**
 * Resumen de rentabilidad directa agregada en el Dashboard Home.
 * <p>
 * Totales monetarios solo incluyen órdenes con estado {@code COMPLETE}.
 * {@code averageMarginPercentage} = totalDirectProfit / totalOrderValue × 100
 * (margen ponderado); {@code null} si totalOrderValue == 0.
 */
public record HomeProfitabilitySummary(
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
