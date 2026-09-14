package com.magyen.platform.home.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Resultado de rentabilidad Home para un período explícito.
 */
public record GetHomeProfitabilityResult(
        LocalDate fromDate,
        LocalDate toDate,
        Instant generatedAt,
        HomeProfitabilitySummary profitabilitySummary
) {
}
