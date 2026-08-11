package com.magyen.platform.finance.presentation.occurrence.request;

import java.time.LocalDateTime;

/**
 * Payload HTTP opcional para pagar una ocurrencia.
 */
public record PayRecurringFinancialObligationOccurrenceRequest(
        LocalDateTime paidAt,
        String observation
) {
}
