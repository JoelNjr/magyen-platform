package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.StartOrderProductionCommand;
import com.magyen.platform.commercial.application.dto.StartOrderProductionResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Transición comercial CONFIRMED → IN_PRODUCTION.
 * <p>
 * No crea ni muta la Orden de Producción.
 */
public class StartOrderProductionUseCase {

    private final OrderRepository orderRepository;

    public StartOrderProductionUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    @Transactional
    public StartOrderProductionResult execute(StartOrderProductionCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.startProduction();
        Order saved = orderRepository.save(order);
        return new StartOrderProductionResult(saved.getId(), saved.getStatus());
    }
}
