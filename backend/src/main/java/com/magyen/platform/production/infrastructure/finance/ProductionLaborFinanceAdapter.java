package com.magyen.platform.production.infrastructure.finance;

import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionLaborPaymentExpenseResult;
import com.magyen.platform.finance.application.usecase.RegisterProductionLaborPaymentExpenseUseCase;
import com.magyen.platform.production.application.port.ProductionLaborFinancePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Production → Finance para registrar el gasto de mano de obra.
 */
public class ProductionLaborFinanceAdapter implements ProductionLaborFinancePort {

    private final RegisterProductionLaborPaymentExpenseUseCase registerProductionLaborPaymentExpenseUseCase;

    public ProductionLaborFinanceAdapter(
            RegisterProductionLaborPaymentExpenseUseCase registerProductionLaborPaymentExpenseUseCase
    ) {
        this.registerProductionLaborPaymentExpenseUseCase = Objects.requireNonNull(
                registerProductionLaborPaymentExpenseUseCase,
                "Register production labor payment expense use case must not be null"
        );
    }

    @Override
    public UUID registerLaborExpense(
            UUID laborWorkId,
            BigDecimal amount,
            LocalDate paymentDate,
            String operatorDisplayName,
            String observation
    ) {
        RegisterProductionLaborPaymentExpenseResult result =
                registerProductionLaborPaymentExpenseUseCase.execute(
                        new RegisterProductionLaborPaymentExpenseCommand(
                                laborWorkId,
                                amount,
                                paymentDate,
                                operatorDisplayName,
                                observation
                        )
                );
        return result.financialTransactionId();
    }
}
