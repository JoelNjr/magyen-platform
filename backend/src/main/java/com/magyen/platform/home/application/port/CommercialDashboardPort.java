package com.magyen.platform.home.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port de lectura comercial para el Dashboard Home.
 * <p>
 * Expone cuentas por cobrar y resumen de rentabilidad directa (SPR-036),
 * sin recalcular costos ni márgenes.
 */
public interface CommercialDashboardPort {

    /**
     * Saldos pendientes actuales ({@code outstandingAmount > 0}).
     */
    HomeReceivablesSnapshot getCurrentOutstandingReceivables();

    /**
     * Resumen de rentabilidad directa de Órdenes comerciales elegibles.
     * <p>
     * Totales monetarios ({@code totalOrderValue}, {@code totalDirectCost},
     * {@code totalDirectProfit}, margen) solo agregan estados {@code COMPLETE}
     * para no tratar costos desconocidos como cero.
     */
    HomeProfitabilitySummarySnapshot getCurrentProfitabilitySummary();

    record HomeReceivablesSnapshot(
            BigDecimal totalOutstandingAmount,
            BigDecimal totalCollectedAmount,
            int orderCount,
            List<ReceivableItem> items
    ) {
    }

    record ReceivableItem(
            UUID orderId,
            String orderNumber,
            UUID customerId,
            BigDecimal orderValue,
            BigDecimal collectedAmount,
            BigDecimal outstandingAmount
    ) {
    }

    record HomeProfitabilitySummarySnapshot(
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
}
