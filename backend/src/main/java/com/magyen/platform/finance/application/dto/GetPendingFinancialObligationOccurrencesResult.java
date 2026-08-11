package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de ocurrencias PENDING con total de compromiso.
 */
public record GetPendingFinancialObligationOccurrencesResult(
        List<FinancialObligationOccurrenceCommitmentResult> occurrences,
        BigDecimal totalPendingAmount
) {
}
