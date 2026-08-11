package com.magyen.platform.finance.presentation.occurrence.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de ocurrencias.
 */
public record GetRecurringFinancialObligationOccurrencesResponse(
        List<RecurringFinancialObligationOccurrenceResponse> occurrences
) {
}
