package com.magyen.platform.production.infrastructure.finance;

import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseCommand;
import com.magyen.platform.finance.application.dto.RegisterProductionAdditionalCostExpenseResult;
import com.magyen.platform.finance.application.usecase.RegisterProductionAdditionalCostExpenseUseCase;
import com.magyen.platform.production.application.port.ProductionAdditionalCostFinancePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Production → Finance para el gasto de un costo directo adicional.
 */
public class ProductionAdditionalCostFinanceAdapter implements ProductionAdditionalCostFinancePort {

    private final RegisterProductionAdditionalCostExpenseUseCase registerProductionAdditionalCostExpenseUseCase;

    public ProductionAdditionalCostFinanceAdapter(
            RegisterProductionAdditionalCostExpenseUseCase registerProductionAdditionalCostExpenseUseCase
    ) {
        this.registerProductionAdditionalCostExpenseUseCase = Objects.requireNonNull(
                registerProductionAdditionalCostExpenseUseCase,
                "Register production additional cost expense use case must not be null"
        );
    }

    @Override
    public UUID registerAdditionalCostExpense(
            UUID additionalCostId,
            BigDecimal amount,
            LocalDate incurredDate,
            String description
    ) {
        RegisterProductionAdditionalCostExpenseResult result =
                registerProductionAdditionalCostExpenseUseCase.execute(
                        new RegisterProductionAdditionalCostExpenseCommand(
                                additionalCostId,
                                amount,
                                incurredDate,
                                description
                        )
                );
        return result.financialTransactionId();
    }
}
