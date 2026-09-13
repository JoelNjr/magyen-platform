package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderPaymentFloorGuard;
import com.magyen.platform.commercial.application.dto.RemoveOrderItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveOrderItemResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Elimina un OrderItem de una Orden editable.
 * <p>
 * No elimina ni muta el QuotationItem de origen. No toca pagos ni producción.
 */
public class RemoveOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final OrderPaymentFloorGuard orderPaymentFloorGuard;

    public RemoveOrderItemUseCase(
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
    public RemoveOrderItemResult execute(RemoveOrderItemCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        Objects.requireNonNull(command.itemId(), "Item id must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.removeItem(command.itemId());
        orderPaymentFloorGuard.ensureTotalCoversAmountPaid(order);

        Order saved = orderRepository.save(order);
        return new RemoveOrderItemResult(
                saved.getId(),
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }
}
