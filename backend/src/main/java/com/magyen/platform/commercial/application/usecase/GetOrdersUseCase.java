package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta las Órdenes existentes.
 */
public class GetOrdersUseCase {

    private final OrderRepository orderRepository;

    public GetOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public GetOrdersResult execute() {
        List<OrderResult> orders = orderRepository.findAll().stream()
                .map(this::toOrderResult)
                .toList();

        return new GetOrdersResult(orders);
    }

    private OrderResult toOrderResult(Order order) {
        return new OrderResult(
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getCustomerId(),
                order.getQuotationId(),
                order.getConfirmationDate(),
                order.getStatus(),
                order.getSalesperson(),
                order.getObservations(),
                order.getTotal().getAmount()
        );
    }
}
