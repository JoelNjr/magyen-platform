package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resumen de movimientos reales del ledger en un período.
 */
public record GetFinancialPeriodSummaryResult(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netResult,
        long transactionCount
) {
}
