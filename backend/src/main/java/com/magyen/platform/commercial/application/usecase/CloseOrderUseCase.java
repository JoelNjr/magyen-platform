package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CloseOrderCommand;
import com.magyen.platform.commercial.application.dto.CloseOrderResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Cierra una Orden entregada cuando la cobranza ya cubre el total.
 * <p>
 * Lee los pagos existentes. No crea un Payment ni un asiento Finance.
 */
public class CloseOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;

    public CloseOrderUseCase(
            OrderRepository orderRepository,
            OrderPaymentCollectionPort orderPaymentCollectionPort
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
    }

    @Transactional
    public CloseOrderResult execute(CloseOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.orderId(), "Order id must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderDomainException(
                    "An order can only be closed from DELIVERED status. Current status: " + order.getStatus()
            );
        }

        BigDecimal collected = collectedAmount(order);
        if (collected.compareTo(order.getTotal().getAmount()) < 0) {
            throw new OrderDomainException(
                    "Collected payments do not cover the order total. Collected: " + collected
                            + ", order total: " + order.getTotal().getAmount()
            );
        }

        if (!order.getPaymentSummary().isFinalPaymentAcknowledged()) {
            order.acknowledgeFinalPayment();
        }
        order.close();
        Order saved = orderRepository.save(order);
        return new CloseOrderResult(
                saved.getId(),
                saved.getStatus(),
                saved.getPaymentSummary().isFinalPaymentAcknowledged()
        );
    }

    private BigDecimal collectedAmount(Order order) {
        OrderPaymentCollectionPort.OrderPaymentCollection collection =
                orderPaymentCollectionPort.getCollection(order.getId());
        if (collection == null || collection.collectedAmount() == null) {
            return BigDecimal.ZERO;
        }
        return collection.collectedAmount();
    }
}
