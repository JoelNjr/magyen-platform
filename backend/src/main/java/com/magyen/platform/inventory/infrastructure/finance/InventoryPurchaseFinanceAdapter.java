package com.magyen.platform.inventory.infrastructure.finance;

import com.magyen.platform.finance.application.dto.EnsureInventoryPurchaseExpenseCommand;
import com.magyen.platform.finance.application.dto.EnsureInventoryPurchaseExpenseResult;
import com.magyen.platform.finance.application.usecase.EnsureInventoryPurchaseExpenseUseCase;
import com.magyen.platform.inventory.application.port.InventoryPurchaseFinancePort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Inventory → Finance para el gasto de caja de una compra de material.
 */
public class InventoryPurchaseFinanceAdapter implements InventoryPurchaseFinancePort {

    private final EnsureInventoryPurchaseExpenseUseCase ensureInventoryPurchaseExpenseUseCase;

    public InventoryPurchaseFinanceAdapter(
            EnsureInventoryPurchaseExpenseUseCase ensureInventoryPurchaseExpenseUseCase
    ) {
        this.ensureInventoryPurchaseExpenseUseCase = Objects.requireNonNull(
                ensureInventoryPurchaseExpenseUseCase,
                "Ensure inventory purchase expense use case must not be null"
        );
    }

    @Override
    public InventoryPurchaseFinanceRecord ensurePurchaseExpense(
            UUID purchaseId,
            BigDecimal amount,
            LocalDate purchaseDate,
            String category,
            String description,
            String observation
    ) {
        EnsureInventoryPurchaseExpenseResult result = ensureInventoryPurchaseExpenseUseCase.execute(
                new EnsureInventoryPurchaseExpenseCommand(
                        purchaseId,
                        amount,
                        purchaseDate,
                        category,
                        description,
                        observation
                )
        );

        return new InventoryPurchaseFinanceRecord(
                result.financialTransactionId(),
                result.amount(),
                result.category(),
                result.alreadyProcessed()
        );
    }
}
