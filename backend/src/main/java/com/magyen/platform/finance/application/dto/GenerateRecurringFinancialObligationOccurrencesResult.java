package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de una generación controlada de ocurrencias.
 */
public record GenerateRecurringFinancialObligationOccurrencesResult(
        LocalDate requestedFrom,
        LocalDate requestedTo,
        int obligationsEvaluated,
        int occurrencesCreated,
        int occurrencesAlreadyExisting,
        int occurrencesSkippedInactive,
        int occurrencesSkippedOutsideValidity,
        List<GetRecurringFinancialObligationOccurrenceResult> createdOccurrences
) {
}
