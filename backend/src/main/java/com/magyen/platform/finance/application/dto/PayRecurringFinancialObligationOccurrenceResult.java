package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del pago de una ocurrencia, incluyendo el movimiento del ledger generado.
 */
public record PayRecurringFinancialObligationOccurrenceResult(
        UUID occurrenceId,
        UUID recurringObligationId,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        RecurringObligationOccurrenceStatus status,
        LocalDateTime paidDate,
        UUID financialTransactionId,
        BigDecimal transactionAmount,
        String transactionCategory
) {
}
