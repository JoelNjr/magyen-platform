package com.magyen.platform.finance.presentation.summary.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Respuesta HTTP del resumen del ledger para un período.
 */
public record GetFinancialPeriodSummaryResponse(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netResult,
        long transactionCount
) {
}
