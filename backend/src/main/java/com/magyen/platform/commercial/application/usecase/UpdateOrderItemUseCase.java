package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderPaymentFloorGuard;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.shared.domain.Money;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Actualiza cantidad y precio unitario de un OrderItem.
 * <p>
 * No cambia quotationItemId, no muta cotización, pagos ni producción.
 */
public class UpdateOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final OrderPaymentFloorGuard orderPaymentFloorGuard;

    public UpdateOrderItemUseCase(
            OrderRepository orderRepository,
            OrderPaymentFloorGuard orderPaymentFloorGuard
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.orderPaymentFloorGuard = Objects.requireNonNull(
                orderPaymentFloorGuard,
                "Order payment floor guard must not be null"
        );
    }

    @Transactional
    public UpdateOrderItemResult execute(UpdateOrderItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.itemId(), "Item id must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.updateItemCommercialCommitment(
                command.itemId(),
                command.quantity(),
                Money.of(command.unitPrice())
        );
        orderPaymentFloorGuard.ensureTotalCoversAmountPaid(order);

        Order saved = orderRepository.save(order);
        OrderItem savedItem = saved.getItems().stream()
                .filter(item -> item.getId().equals(command.itemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Order item missing after save: " + command.itemId()));

        return new UpdateOrderItemResult(
                saved.getId(),
                savedItem.getId(),
                savedItem.getQuantity(),
                savedItem.getUnitPrice().getAmount(),
                savedItem.getSubtotal().getAmount(),
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }
}
