package com.magyen.platform.finance.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entrada del caso de uso para pagar una ocurrencia pendiente.
 * <p>
 * {@code paidAt} es opcional; si se omite se usa la marca de tiempo actual.
 */
public record PayRecurringFinancialObligationOccurrenceCommand(
        UUID occurrenceId,
        LocalDateTime paidAt,
        String observation
) {
    public PayRecurringFinancialObligationOccurrenceCommand(UUID occurrenceId) {
        this(occurrenceId, null, null);
    }
}
