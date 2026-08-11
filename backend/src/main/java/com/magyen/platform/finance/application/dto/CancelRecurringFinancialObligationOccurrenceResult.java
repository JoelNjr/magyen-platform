package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;

import java.util.UUID;

/**
 * Resultado de cancelación de una ocurrencia.
 */
public record CancelRecurringFinancialObligationOccurrenceResult(
        UUID occurrenceId,
        RecurringObligationOccurrenceStatus status
) {
}
