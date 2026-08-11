package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de creación de una ocurrencia de obligación recurrente.
 */
public record CreateRecurringFinancialObligationOccurrenceResult(
        UUID occurrenceId,
        UUID recurringObligationId,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        RecurringObligationOccurrenceStatus status,
        LocalDateTime paidDate,
        UUID financialTransactionId,
        String observation
) {
}
