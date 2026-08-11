package com.magyen.platform.finance.presentation.occurrence.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para crear una ocurrencia de obligación recurrente.
 */
public record CreateRecurringFinancialObligationOccurrenceRequest(
        UUID recurringObligationId,
        LocalDate dueDate,
        String observation
) {
}
