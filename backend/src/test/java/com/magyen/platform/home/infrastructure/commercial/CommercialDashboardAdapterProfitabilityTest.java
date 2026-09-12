package com.magyen.platform.home.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityByPromisedDeliveryPeriodQuery;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityByPromisedDeliveryPeriodUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.home.application.port.CommercialDashboardPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommercialDashboardAdapterProfitabilityTest {

    @Mock
    private GetOrdersUseCase getOrdersUseCase;

    @Mock
    private OrderPaymentCollectionPort orderPaymentCollectionPort;

    @Mock
    private GetOrderProfitabilityByPromisedDeliveryPeriodUseCase periodUseCase;

    @Test
    void mapsPeriodSummaryWithoutRecalculatingFormulas() {
        LocalDate fromDate = LocalDate.of(2026, 9, 1);
        LocalDate toDate = LocalDate.of(2026, 9, 30);
        when(periodUseCase.execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(fromDate, toDate)))
                .thenReturn(new OrderProfitabilitySummary(
                        3,
                        1,
                        1,
                        1,
                        new BigDecimal("1000000.00"),
                        new BigDecimal("240000.00"),
                        new BigDecimal("760000.00"),
                        new BigDecimal("76.00"),
                        2
                ));

        CommercialDashboardPort.HomeProfitabilitySummarySnapshot snapshot =
                new CommercialDashboardAdapter(getOrdersUseCase, orderPaymentCollectionPort, periodUseCase)
                        .getProfitabilitySummary(fromDate, toDate);

        assertEquals(3, snapshot.evaluatedOrderCount());
        assertEquals(1, snapshot.completeOrderCount());
        assertEquals(1, snapshot.partiallyUnvaluedOrderCount());
        assertEquals(1, snapshot.noCostDataOrderCount());
        assertEquals(0, snapshot.totalOrderValue().compareTo(new BigDecimal("1000000.00")));
        assertEquals(0, snapshot.totalDirectCost().compareTo(new BigDecimal("240000.00")));
        assertEquals(0, snapshot.totalDirectProfit().compareTo(new BigDecimal("760000.00")));
        assertEquals(0, snapshot.averageMarginPercentage().compareTo(new BigDecimal("76.00")));
        assertEquals(2, snapshot.unvaluedCostCount());
        verify(periodUseCase).execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(fromDate, toDate));
    }

    @Test
    void zeroCompleteOrderValueYieldsNullAverageMargin() {
        LocalDate fromDate = LocalDate.of(2026, 9, 1);
        LocalDate toDate = LocalDate.of(2026, 9, 30);
        when(periodUseCase.execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(fromDate, toDate)))
                .thenReturn(new OrderProfitabilitySummary(
                        1,
                        1,
                        0,
                        0,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        0
                ));

        CommercialDashboardPort.HomeProfitabilitySummarySnapshot snapshot =
                new CommercialDashboardAdapter(getOrdersUseCase, orderPaymentCollectionPort, periodUseCase)
                        .getProfitabilitySummary(fromDate, toDate);

        assertEquals(1, snapshot.completeOrderCount());
        assertEquals(0, snapshot.totalOrderValue().compareTo(BigDecimal.ZERO));
        assertNull(snapshot.averageMarginPercentage());
    }
}
