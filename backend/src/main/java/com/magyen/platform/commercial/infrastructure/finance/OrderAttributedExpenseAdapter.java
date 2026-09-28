package com.magyen.platform.commercial.infrastructure.finance;

import com.magyen.platform.commercial.application.port.OrderAttributedExpensePort;
import com.magyen.platform.finance.application.usecase.SumManualOrderExpensesUseCase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Commercial → Finance para gastos manuales atribuidos a una Orden.
 */
public class OrderAttributedExpenseAdapter implements OrderAttributedExpensePort {

    private final SumManualOrderExpensesUseCase sumManualOrderExpensesUseCase;

    public OrderAttributedExpenseAdapter(SumManualOrderExpensesUseCase sumManualOrderExpensesUseCase) {
        this.sumManualOrderExpensesUseCase = Objects.requireNonNull(
                sumManualOrderExpensesUseCase,
                "Sum manual order expenses use case must not be null"
        );
    }

    @Override
    public BigDecimal sumManualExpenses(UUID orderId) {
        return sumManualOrderExpensesUseCase.execute(orderId);
    }
}
