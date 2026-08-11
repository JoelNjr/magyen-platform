package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado del listado de ocurrencias de obligaciones recurrentes.
 */
public record GetRecurringFinancialObligationOccurrencesResult(
        List<GetRecurringFinancialObligationOccurrenceResult> occurrences
) {
}
