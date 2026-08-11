package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado de rentabilidad directa de una Orden.
 * <p>
 * {@code orderValue} es {@code Order.getTotal()} (valor comprometido), no el efectivo cobrado.
 * {@code plotterMaterialCost} queda en cero y {@code plotterCostAttributable=false}
 * hasta que PlotterJob exponga orderId confiable (diferido).
 */
public record GetOrderProfitabilityResult(
        UUID orderId,
        BigDecimal orderValue,
        BigDecimal collectedAmount,
        BigDecimal outstandingAmount,
        BigDecimal materialCost,
        BigDecimal laborCost,
        BigDecimal plotterMaterialCost,
        boolean plotterCostAttributable,
        BigDecimal totalDirectCost,
        BigDecimal directProfit,
        BigDecimal directMarginPercentage,
        int unvaluedMaterialConsumptionCount,
        OrderProfitabilityStatus profitabilityStatus
) {
}
