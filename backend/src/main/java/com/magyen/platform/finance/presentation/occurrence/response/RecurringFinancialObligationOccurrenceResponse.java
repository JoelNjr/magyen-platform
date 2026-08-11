package com.magyen.platform.finance.presentation.occurrence.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación HTTP de una ocurrencia de obligación recurrente.
 */
public record RecurringFinancialObligationOccurrenceResponse(
        UUID occurrenceId,
        UUID recurringObligationId,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        String status,
        LocalDateTime paidDate,
        UUID financialTransactionId,
        String observation
) {
}
