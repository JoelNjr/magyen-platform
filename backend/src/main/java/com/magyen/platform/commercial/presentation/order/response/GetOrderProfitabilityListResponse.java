package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Listado HTTP de rentabilidad individual y el mismo resumen ponderado que Home.
 */
public record GetOrderProfitabilityListResponse(
        List<GetOrderProfitabilityResponse> orders,
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
