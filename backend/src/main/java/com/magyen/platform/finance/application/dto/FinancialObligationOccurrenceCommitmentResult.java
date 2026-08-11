package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.RecurringObligationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vista de compromiso (PENDING) enriquecida para consultas de lectura Finance.
 */
public record FinancialObligationOccurrenceCommitmentResult(
        UUID occurrenceId,
        UUID recurringObligationId,
        String obligationName,
        RecurringObligationType obligationType,
        LocalDate dueDate,
        BigDecimal expectedAmount,
        RecurringObligationOccurrenceStatus status,
        boolean overdue,
        Integer daysUntilDue,
        Integer daysOverdue
) {
}
