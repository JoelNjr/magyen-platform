package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para cancelar una ocurrencia pendiente.
 */
public record CancelRecurringFinancialObligationOccurrenceCommand(
        UUID occurrenceId
) {
}
