package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de rentabilidad directa de una Orden.
 * <p>
 * {@code orderValue} es {@code Order.getTotal()} (valor comprometido), no el efectivo cobrado.
 * {@code plotterMaterialCost} es el snapshot histórico de papel INTERNAL_MAGYEN.
 * {@code plotterCostAttributable=true} cuando hay trabajos internos y todos tienen costo valorado.
 * Las etiquetas de pedido/cliente son de lectura; no son UUIDs de negocio.
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
        OrderProfitabilityStatus profitabilityStatus,
        String orderNumber,
        String description,
        String customerName,
        LocalDate promisedDeliveryDate
) {
    public GetOrderProfitabilityResult(
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
        this(
                orderId,
                orderValue,
                collectedAmount,
                outstandingAmount,
                materialCost,
                laborCost,
                plotterMaterialCost,
                plotterCostAttributable,
                totalDirectCost,
                directProfit,
                directMarginPercentage,
                unvaluedMaterialConsumptionCount,
                profitabilityStatus,
                null,
                null,
                null,
                null
        );
    }
}
