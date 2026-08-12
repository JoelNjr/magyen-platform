package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;

/**
 * Resumen financiero expuesto por el Dashboard Home.
 */
public record HomeFinancialSummaryResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal netResult,
        long transactionCount
) {
}
