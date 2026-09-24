package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryCommand;
import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Transición comercial IN_PRODUCTION → READY_FOR_DELIVERY.
 * <p>
 * No completa ni muta la Orden de Producción.
 */
public class MarkOrderReadyForDeliveryUseCase {

    private final OrderRepository orderRepository;

    public MarkOrderReadyForDeliveryUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    @Transactional
    public MarkOrderReadyForDeliveryResult execute(MarkOrderReadyForDeliveryCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.markReadyForDelivery();
        Order saved = orderRepository.save(order);
        return new MarkOrderReadyForDeliveryResult(saved.getId(), saved.getStatus());
    }
}
