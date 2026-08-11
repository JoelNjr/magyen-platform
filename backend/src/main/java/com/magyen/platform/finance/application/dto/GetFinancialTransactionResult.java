package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de consulta de un movimiento financiero.
 */
public record GetFinancialTransactionResult(
        UUID transactionId,
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
