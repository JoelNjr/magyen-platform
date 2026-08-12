package com.magyen.platform.home.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.home.application.port.CommercialDashboardPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommercialDashboardAdapterProfitabilityTest {

    @Mock
    private GetOrdersUseCase getOrdersUseCase;

    @Mock
    private OrderPaymentCollectionPort orderPaymentCollectionPort;

    @Mock
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Test
    void aggregatesCompleteOnlyAndDoesNotTreatUnknownCostsAsMoneyTotals() {
        UUID completeId = UUID.randomUUID();
        UUID partialId = UUID.randomUUID();
        UUID noCostId = UUID.randomUUID();
        UUID closedId = UUID.randomUUID();

        when(getOrdersUseCase.execute()).thenReturn(new GetOrdersResult(List.of(
                order(completeId, OrderStatus.CONFIRMED, "1000000.00"),
                order(partialId, OrderStatus.IN_PRODUCTION, "500000.00"),
                order(noCostId, OrderStatus.DELIVERED, "250000.00"),
                order(closedId, OrderStatus.CLOSED, "999000.00")
        )));

        when(getOrderProfitabilityUseCase.execute(any())).thenAnswer(invocation -> {
            UUID orderId = invocation.getArgument(0, com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery.class)
                    .orderId();
            if (orderId.equals(completeId)) {
                return profitability(
                        orderId,
                        "1000000.00",
                        "240000.00",
                        "760000.00",
                        "76.00",
                        0,
                        OrderProfitabilityStatus.COMPLETE
                );
            }
            if (orderId.equals(partialId)) {
                return profitability(
                        orderId,
                        "500000.00",
                        "150000.00",
                        "350000.00",
                        "70.00",
                        2,
                        OrderProfitabilityStatus.PARTIALLY_UNVALUED
                );
            }
            return profitability(
                    orderId,
                    "250000.00",
                    "0.00",
                    "250000.00",
                    "100.00",
                    0,
                    OrderProfitabilityStatus.NO_COST_DATA
            );
        });

        CommercialDashboardPort.HomeProfitabilitySummarySnapshot snapshot =
                new CommercialDashboardAdapter(
                        getOrdersUseCase,
                        orderPaymentCollectionPort,
                        getOrderProfitabilityUseCase
                ).getCurrentProfitabilitySummary();

        assertEquals(3, snapshot.evaluatedOrderCount());
        assertEquals(1, snapshot.completeOrderCount());
        assertEquals(1, snapshot.partiallyUnvaluedOrderCount());
        assertEquals(1, snapshot.noCostDataOrderCount());
        assertEquals(0, snapshot.totalOrderValue().compareTo(new BigDecimal("1000000.00")));
        assertEquals(0, snapshot.totalDirectCost().compareTo(new BigDecimal("240000.00")));
        assertEquals(0, snapshot.totalDirectProfit().compareTo(new BigDecimal("760000.00")));
        assertEquals(0, snapshot.averageMarginPercentage().compareTo(new BigDecimal("76.00")));
        assertEquals(2, snapshot.unvaluedCostCount());
    }

    @Test
    void zeroCompleteOrderValueYieldsNullAverageMargin() {
        UUID orderId = UUID.randomUUID();
        when(getOrdersUseCase.execute()).thenReturn(new GetOrdersResult(List.of(
                order(orderId, OrderStatus.CONFIRMED, "0.00")
        )));
        when(getOrderProfitabilityUseCase.execute(any())).thenReturn(profitability(
                orderId,
                "0.00",
                "0.00",
                "0.00",
                null,
                0,
                OrderProfitabilityStatus.COMPLETE
        ));

        CommercialDashboardPort.HomeProfitabilitySummarySnapshot snapshot =
                new CommercialDashboardAdapter(
                        getOrdersUseCase,
                        orderPaymentCollectionPort,
                        getOrderProfitabilityUseCase
                ).getCurrentProfitabilitySummary();

        assertEquals(1, snapshot.completeOrderCount());
        assertEquals(0, snapshot.totalOrderValue().compareTo(BigDecimal.ZERO));
        assertNull(snapshot.averageMarginPercentage());
    }

    private static OrderResult order(UUID orderId, OrderStatus status, String total) {
        return new OrderResult(
                orderId,
                "ORD-" + orderId.toString().substring(0, 8),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 1),
                status,
                "Tester",
                null,
                new BigDecimal(total)
        );
    }

    private static GetOrderProfitabilityResult profitability(
            UUID orderId,
            String orderValue,
            String totalDirectCost,
            String directProfit,
            String margin,
            int unvaluedCount,
            OrderProfitabilityStatus status
    ) {
        return new GetOrderProfitabilityResult(
                orderId,
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
                unvaluedCount,
                status
        );
    }
}
