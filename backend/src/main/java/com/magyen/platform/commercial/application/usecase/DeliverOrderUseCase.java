package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.DeliverOrderCommand;
import com.magyen.platform.commercial.application.dto.DeliverOrderResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.exception.OrderDeliveryDateConflictException;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Registra la entrega real de una Orden comercial.
 * <p>
 * La fecha de negocio llega en el comando. El reloj solo define el día máximo.
 * No crea pagos, asientos Finance, producción ni comisión.
 */
public class DeliverOrderUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public DeliverOrderUseCase(OrderRepository orderRepository, Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Transactional
    public DeliverOrderResult execute(DeliverOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.deliveryDate(), "Delivery date must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new OrderDomainException("A closed order cannot be delivered");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            if (command.deliveryDate().equals(order.getActualDeliveryDate())) {
                return toResult(order);
            }
            throw new OrderDeliveryDateConflictException(
                    "Delivery date does not match the stored actual delivery date"
            );
        }

        LocalDate businessToday = LocalDate.now(clock);
        order.deliver(command.deliveryDate(), businessToday);
        return toResult(orderRepository.save(order));
    }

    private static DeliverOrderResult toResult(Order order) {
        return new DeliverOrderResult(order.getId(), order.getStatus(), order.getActualDeliveryDate());
    }
}
