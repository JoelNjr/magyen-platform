package com.magyen.platform.commercial.infrastructure.finance;

import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderQuery;
import com.magyen.platform.finance.application.dto.GetPaymentsByOrderResult;
import com.magyen.platform.finance.application.usecase.GetPaymentsByOrderUseCase;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Commercial → Finance para cobranza de una Orden (solo lectura).
 */
public class OrderPaymentCollectionAdapter implements OrderPaymentCollectionPort {

    private final GetPaymentsByOrderUseCase getPaymentsByOrderUseCase;

    public OrderPaymentCollectionAdapter(GetPaymentsByOrderUseCase getPaymentsByOrderUseCase) {
        this.getPaymentsByOrderUseCase = Objects.requireNonNull(
                getPaymentsByOrderUseCase,
                "Get payments by order use case must not be null"
        );
    }

    @Override
    public OrderPaymentCollection getCollection(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        GetPaymentsByOrderResult result = getPaymentsByOrderUseCase.execute(
                new GetPaymentsByOrderQuery(orderId)
        );

        return new OrderPaymentCollection(
                result.totalPaid(),
                result.remainingBalance()
        );
    }
}
