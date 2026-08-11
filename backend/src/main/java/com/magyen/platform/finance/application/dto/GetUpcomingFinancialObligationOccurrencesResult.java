package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado de ocurrencias próximas (hoy o futuro cercano). No incluye vencidas.
 */
public record GetUpcomingFinancialObligationOccurrencesResult(
        List<FinancialObligationOccurrenceCommitmentResult> occurrences
) {
}
