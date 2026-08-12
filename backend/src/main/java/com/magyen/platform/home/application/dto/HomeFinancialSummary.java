package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;

/**
 * Resumen financiero del período para el Dashboard Home (solo lectura).
 */
public record HomeFinancialSummary(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal netResult,
        long transactionCount
) {
}
