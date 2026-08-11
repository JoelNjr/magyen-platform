package com.magyen.platform.finance.presentation.occurrence.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP tras pagar una ocurrencia.
 */
public record PayRecurringFinancialObligationOccurrenceResponse(
        UUID occurrenceId,
        UUID recurringObligationId,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        String status,
        LocalDateTime paidDate,
        UUID financialTransactionId,
        BigDecimal transactionAmount,
        String transactionCategory
) {
}
