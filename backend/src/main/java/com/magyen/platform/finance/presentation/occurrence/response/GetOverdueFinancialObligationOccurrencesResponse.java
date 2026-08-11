package com.magyen.platform.finance.presentation.occurrence.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta HTTP de ocurrencias vencidas con total overdue.
 */
public record GetOverdueFinancialObligationOccurrencesResponse(
        List<FinancialObligationOccurrenceCommitmentResponse> occurrences,
        BigDecimal totalOverdueAmount
) {
}
