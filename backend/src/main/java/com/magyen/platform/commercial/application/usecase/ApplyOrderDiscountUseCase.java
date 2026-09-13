package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderPaymentFloorGuard;
import com.magyen.platform.commercial.application.dto.ApplyOrderDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApplyOrderDiscountResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Aplica descuento comercial sobre el subtotal de la Orden.
 * <p>
 * No altera precios unitarios, pagos ni producción.
 */
public class ApplyOrderDiscountUseCase {

    private final OrderRepository orderRepository;
    private final OrderPaymentFloorGuard orderPaymentFloorGuard;

    public ApplyOrderDiscountUseCase(
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
    public ApplyOrderDiscountResult execute(ApplyOrderDiscountCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
        if (command.discountAmount() == null) {
            throw new OrderDomainException("Discount amount must not be null");
        }

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        order.applyDiscount(Money.of(command.discountAmount()));
        orderPaymentFloorGuard.ensureTotalCoversAmountPaid(order);

        Order saved = orderRepository.save(order);
        return new ApplyOrderDiscountResult(
                saved.getId(),
                saved.getSubtotal().getAmount(),
                saved.getDiscount().getAmount(),
                saved.getTotal().getAmount()
        );
    }
}
