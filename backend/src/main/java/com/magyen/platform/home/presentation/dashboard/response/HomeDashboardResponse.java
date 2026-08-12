package com.magyen.platform.home.presentation.dashboard.response;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Respuesta HTTP del Dashboard Home.
 */
public record HomeDashboardResponse(
        LocalDate fromDate,
        LocalDate toDate,
        Instant generatedAt,
        HomeFinancialSummaryResponse financialSummary,
        HomeReceivablesResponse receivables,
        HomeCommitmentsResponse commitments,
        HomeInventoryAlertsResponse inventoryAlerts,
        HomePaperRollAlertsResponse paperRollAlerts,
        HomeProductionSummaryResponse productionSummary,
        HomeProfitabilitySummaryResponse profitabilitySummary
) {
}
