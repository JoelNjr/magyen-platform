package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de una ocurrencia por identidad.
 */
public record GetRecurringFinancialObligationOccurrenceQuery(
        UUID occurrenceId
) {
}
