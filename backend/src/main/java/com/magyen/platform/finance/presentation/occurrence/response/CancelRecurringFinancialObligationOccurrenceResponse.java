package com.magyen.platform.finance.presentation.occurrence.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras cancelar una ocurrencia.
 */
public record CancelRecurringFinancialObligationOccurrenceResponse(
        UUID occurrenceId,
        String status
) {
}
