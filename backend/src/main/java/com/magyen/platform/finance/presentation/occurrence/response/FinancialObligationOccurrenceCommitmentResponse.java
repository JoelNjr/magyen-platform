package com.magyen.platform.finance.presentation.occurrence.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP de un compromiso PENDING enriquecido.
 */
public record FinancialObligationOccurrenceCommitmentResponse(
        UUID occurrenceId,
        UUID recurringObligationId,
        String obligationName,
        String obligationType,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        String status,
        boolean overdue,
        Integer daysUntilDue,
        Integer daysOverdue
) {
}
