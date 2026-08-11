package com.magyen.platform.finance.presentation.transaction.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para registrar un movimiento en el ledger financiero.
 * <p>
 * {@code sourceType} es opcional: si se omite, se interpreta como {@code MANUAL}.
 */
public record RegisterFinancialTransactionRequest(
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
