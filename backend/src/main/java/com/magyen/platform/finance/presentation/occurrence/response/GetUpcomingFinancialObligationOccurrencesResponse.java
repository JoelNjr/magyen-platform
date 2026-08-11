package com.magyen.platform.finance.presentation.occurrence.response;

import java.util.List;

/**
 * Respuesta HTTP de ocurrencias próximas (hoy o futuro cercano).
 */
public record GetUpcomingFinancialObligationOccurrencesResponse(
        List<FinancialObligationOccurrenceCommitmentResponse> occurrences
) {
}
