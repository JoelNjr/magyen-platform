package com.magyen.platform.finance.presentation.occurrence.request;

import java.time.LocalDate;

/**
 * Payload HTTP para generar ocurrencias de obligaciones en un rango de fechas.
 */
public record GenerateRecurringFinancialObligationOccurrencesRequest(
        LocalDate fromDate,
        LocalDate toDate
) {
}
