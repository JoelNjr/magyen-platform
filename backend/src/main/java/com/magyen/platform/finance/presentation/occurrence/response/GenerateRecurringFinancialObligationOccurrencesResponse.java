package com.magyen.platform.finance.presentation.occurrence.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta HTTP del resumen de generación de ocurrencias.
 */
public record GenerateRecurringFinancialObligationOccurrencesResponse(
        LocalDate requestedFrom,
        LocalDate requestedTo,
        int obligationsEvaluated,
        int occurrencesCreated,
        int occurrencesAlreadyExisting,
        int occurrencesSkippedInactive,
        int occurrencesSkippedOutsideValidity,
        List<RecurringFinancialObligationOccurrenceResponse> createdOccurrences
) {
}
