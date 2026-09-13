package com.magyen.platform.commercial.application;

import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valida el piso de pagos contra cobranza Finance. No modifica Payment ni el ledger.
 */
public class OrderPaymentFloorGuard {

    private final OrderPaymentCollectionPort orderPaymentCollectionPort;

    public OrderPaymentFloorGuard(OrderPaymentCollectionPort orderPaymentCollectionPort) {
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
    }

    public void ensureTotalCoversAmountPaid(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        BigDecimal collectedAmount = orderPaymentCollectionPort.getCollection(order.getId()).collectedAmount();
        Money amountAlreadyPaid = collectedAmount == null ? Money.zero() : Money.of(collectedAmount);
        order.ensureTotalCoversAmountPaid(amountAlreadyPaid);
    }
}
