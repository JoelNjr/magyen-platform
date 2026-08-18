package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderProfitabilityAggregatorTest {

    @Test
    void weightedMarginUsesTotalsNotAverageOfPercentages() {
        GetOrderProfitabilityResult highMargin = complete(
                "1000.00",
                "100.00",
                "900.00",
                "90.00"
        );
        GetOrderProfitabilityResult lowMargin = complete(
                "100.00",
                "80.00",
                "20.00",
                "20.00"
        );

        OrderProfitabilitySummary summary = OrderProfitabilityAggregator.summarize(List.of(highMargin, lowMargin));

        assertEquals(2, summary.evaluatedOrderCount());
        assertEquals(2, summary.completeOrderCount());
        assertEquals(new BigDecimal("1100.00"), summary.totalOrderValue());
        assertEquals(new BigDecimal("180.00"), summary.totalDirectCost());
        assertEquals(new BigDecimal("920.00"), summary.totalDirectProfit());
        assertEquals(new BigDecimal("83.64"), summary.weightedMarginPercentage());
    }

    @Test
    void excludesNonCompleteOrdersFromMoneyTotals() {
        GetOrderProfitabilityResult complete = complete("500.00", "100.00", "400.00", "80.00");
        GetOrderProfitabilityResult noCost = result(
                "200.00",
                "0.00",
                "200.00",
                "100.00",
                OrderProfitabilityStatus.NO_COST_DATA
        );

        OrderProfitabilitySummary summary = OrderProfitabilityAggregator.summarize(List.of(complete, noCost));

        assertEquals(2, summary.evaluatedOrderCount());
        assertEquals(1, summary.completeOrderCount());
        assertEquals(1, summary.noCostDataOrderCount());
        assertEquals(new BigDecimal("500.00"), summary.totalOrderValue());
        assertEquals(new BigDecimal("80.00"), summary.weightedMarginPercentage());
    }

    @Test
    void nullWeightedMarginWhenCompleteOrderValueIsZero() {
        OrderProfitabilitySummary summary = OrderProfitabilityAggregator.summarize(List.of(
                complete("0.00", "0.00", "0.00", null)
        ));
        assertNull(summary.weightedMarginPercentage());
    }

    private static GetOrderProfitabilityResult complete(
            String orderValue,
            String totalDirectCost,
            String directProfit,
            String margin
    ) {
        return result(
                orderValue,
                totalDirectCost,
                directProfit,
                margin,
                OrderProfitabilityStatus.COMPLETE
        );
    }

    private static GetOrderProfitabilityResult result(
            String orderValue,
            String totalDirectCost,
            String directProfit,
            String margin,
            OrderProfitabilityStatus status
    ) {
        return new GetOrderProfitabilityResult(
                UUID.randomUUID(),
                new BigDecimal(orderValue),
                BigDecimal.ZERO,
                new BigDecimal(orderValue),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                new BigDecimal(totalDirectCost),
                new BigDecimal(directProfit),
                margin == null ? null : new BigDecimal(margin),
                0,
                status
        );
    }
}
