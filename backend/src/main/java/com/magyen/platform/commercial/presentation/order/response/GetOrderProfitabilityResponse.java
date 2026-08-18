package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP de rentabilidad directa de una Orden.
 */
public record GetOrderProfitabilityResponse(
        UUID orderId,
        String orderNumber,
        String description,
        String customerName,
        LocalDate promisedDeliveryDate,
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
        String profitabilityStatus
) {
}
