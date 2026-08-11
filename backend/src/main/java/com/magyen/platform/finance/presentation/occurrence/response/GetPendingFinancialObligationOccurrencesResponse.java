package com.magyen.platform.finance.presentation.occurrence.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta HTTP de ocurrencias PENDING con total de compromiso.
 */
public record GetPendingFinancialObligationOccurrencesResponse(
        List<FinancialObligationOccurrenceCommitmentResponse> occurrences,
        BigDecimal totalPendingAmount
) {
}
