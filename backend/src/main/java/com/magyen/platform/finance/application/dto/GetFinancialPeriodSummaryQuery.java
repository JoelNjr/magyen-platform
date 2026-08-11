package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;

/**
 * Consulta de resumen del ledger para un período inclusivo.
 */
public record GetFinancialPeriodSummaryQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
