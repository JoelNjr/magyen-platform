package com.magyen.platform.home.presentation.dashboard.response;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta HTTP de rentabilidad Home para un período.
 */
public record HomeProfitabilityResponse(
        LocalDate fromDate,
        LocalDate toDate,
        Instant generatedAt,
        HomeProfitabilitySummaryResponse profitabilitySummary
) {
}
