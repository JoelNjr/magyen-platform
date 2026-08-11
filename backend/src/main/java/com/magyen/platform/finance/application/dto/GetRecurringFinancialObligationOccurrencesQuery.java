package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;

/**
 * Consulta de listado de ocurrencias. {@code status} opcional filtra por estado.
 */
public record GetRecurringFinancialObligationOccurrencesQuery(
        RecurringObligationOccurrenceStatus status
) {
    public static GetRecurringFinancialObligationOccurrencesQuery all() {
        return new GetRecurringFinancialObligationOccurrencesQuery(null);
    }
}
