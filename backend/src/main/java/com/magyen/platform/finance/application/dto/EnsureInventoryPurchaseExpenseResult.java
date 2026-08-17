package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del EXPENSE de caja por compra de inventario.
 */
public record EnsureInventoryPurchaseExpenseResult(
        UUID financialTransactionId,
        UUID purchaseId,
        BigDecimal amount,
        String category,
        boolean alreadyProcessed
) {
}
