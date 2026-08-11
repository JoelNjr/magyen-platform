package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una ocurrencia de obligación recurrente.
 */
public record CreateRecurringFinancialObligationOccurrenceCommand(
        UUID recurringObligationId,
        LocalDate dueDate,
        String observation
) {
}
