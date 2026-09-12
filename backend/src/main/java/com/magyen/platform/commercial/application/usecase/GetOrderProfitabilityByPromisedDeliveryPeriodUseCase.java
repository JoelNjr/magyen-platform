package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderProfitabilityAggregator;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityByPromisedDeliveryPeriodQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderProfitabilityEligibility;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Resume la rentabilidad de órdenes cuya entrega programada cae en el período.
 * <p>
 * El período elige qué órdenes participan. No recalcula fórmulas ni altera
 * {@link GetOrderProfitabilityListUseCase} (listado all-time).
 */
public class GetOrderProfitabilityByPromisedDeliveryPeriodUseCase {

    private final OrderRepository orderRepository;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    public GetOrderProfitabilityByPromisedDeliveryPeriodUseCase(
            OrderRepository orderRepository,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.getOrderProfitabilityUseCase = Objects.requireNonNull(
                getOrderProfitabilityUseCase,
                "Get order profitability use case must not be null"
        );
    }

    public OrderProfitabilitySummary execute(GetOrderProfitabilityByPromisedDeliveryPeriodQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateRange(query.fromDate(), query.toDate());

        List<Order> ordersInPeriod = orderRepository.findByPromisedDeliveryDateBetween(
                query.fromDate(),
                query.toDate()
        );

        return OrderProfitabilityAggregator.summarize(
                ordersInPeriod.stream()
                        .filter(order -> OrderProfitabilityEligibility.includes(order.getStatus()))
                        .map(order -> getOrderProfitabilityUseCase.execute(
                                new GetOrderProfitabilityQuery(order.getId())
                        ))
                        .toList()
        );
    }

    private static void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new OrderDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new OrderDomainException("From date must not be after to date");
        }
    }
}
