package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Agrega resultados individuales de {@link com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase}.
 * <p>
 * Una sola regla para Home y para la vista individual. No promedia márgenes porcentuales.
 */
public final class OrderProfitabilityAggregator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private OrderProfitabilityAggregator() {
    }

    public static OrderProfitabilitySummary summarize(List<GetOrderProfitabilityResult> results) {
        Objects.requireNonNull(results, "Profitability results must not be null");

        int completeCount = 0;
        int partiallyUnvaluedCount = 0;
        int noCostDataCount = 0;
        int unvaluedCostCount = 0;
        BigDecimal totalOrderValue = BigDecimal.ZERO;
        BigDecimal totalDirectCost = BigDecimal.ZERO;
        BigDecimal totalDirectProfit = BigDecimal.ZERO;

        for (GetOrderProfitabilityResult profitability : results) {
            unvaluedCostCount += Math.max(0, profitability.unvaluedMaterialConsumptionCount());
            OrderProfitabilityStatus status = profitability.profitabilityStatus();
            if (status == OrderProfitabilityStatus.COMPLETE) {
                completeCount++;
                totalOrderValue = totalOrderValue.add(nullToZero(profitability.orderValue()));
                totalDirectCost = totalDirectCost.add(nullToZero(profitability.totalDirectCost()));
                totalDirectProfit = totalDirectProfit.add(nullToZero(profitability.directProfit()));
            } else if (status == OrderProfitabilityStatus.PARTIALLY_UNVALUED) {
                partiallyUnvaluedCount++;
            } else if (status == OrderProfitabilityStatus.NO_COST_DATA) {
                noCostDataCount++;
            }
        }

        BigDecimal weightedMargin = null;
        if (totalOrderValue.compareTo(BigDecimal.ZERO) > 0) {
            weightedMargin = totalDirectProfit
                    .divide(totalOrderValue, 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new OrderProfitabilitySummary(
                results.size(),
                completeCount,
                partiallyUnvaluedCount,
                noCostDataCount,
                totalOrderValue,
                totalDirectCost,
                totalDirectProfit,
                weightedMargin,
                unvaluedCostCount
        );
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
