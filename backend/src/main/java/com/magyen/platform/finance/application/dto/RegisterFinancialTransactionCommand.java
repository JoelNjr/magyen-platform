package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un movimiento en el ledger financiero.
 */
public record RegisterFinancialTransactionCommand(
        FinancialTransactionType type,
        BigDecimal amount,
        LocalDate transactionDate,
        String category,
        String description,
        String observation,
        FinancialTransactionSourceType sourceType,
        UUID sourceId
) {
}
