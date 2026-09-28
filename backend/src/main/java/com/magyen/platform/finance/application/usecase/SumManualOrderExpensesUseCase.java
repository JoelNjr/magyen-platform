package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Suma los gastos manuales atribuidos a una Orden.
 * <p>
 * Lee el mismo movimiento del ledger. No incluye orígenes que la rentabilidad
 * ya toma por producción, Plotter o mano de obra, ni movimientos sin pedido.
 */
public class SumManualOrderExpensesUseCase {

    private final FinancialTransactionRepository financialTransactionRepository;

    public SumManualOrderExpensesUseCase(FinancialTransactionRepository financialTransactionRepository) {
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    public BigDecimal execute(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");
        BigDecimal total = financialTransactionRepository.findByOrderId(orderId).stream()
                .filter(this::isAttributedManualExpense)
                .map(transaction -> transaction.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isAttributedManualExpense(FinancialTransaction transaction) {
        return transaction.getType() == FinancialTransactionType.EXPENSE
                && transaction.getSourceType() == FinancialTransactionSourceType.MANUAL
                && transaction.getOrderId() != null;
    }
}
