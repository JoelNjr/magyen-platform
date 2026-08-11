package com.magyen.platform.finance.presentation.transaction.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación HTTP de un movimiento del ledger financiero.
 */
public record FinancialTransactionResponse(
        UUID transactionId,
        String type,
        BigDecimal amount,
        LocalDate transactionDate,
        String category,
        String description,
        String observation,
        String sourceType,
        UUID sourceId
) {
}
