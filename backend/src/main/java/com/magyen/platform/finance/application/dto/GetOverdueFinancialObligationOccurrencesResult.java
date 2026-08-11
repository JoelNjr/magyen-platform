package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de ocurrencias vencidas con total overdue.
 */
public record GetOverdueFinancialObligationOccurrencesResult(
        List<FinancialObligationOccurrenceCommitmentResult> occurrences,
        BigDecimal totalOverdueAmount
) {
}
