package com.magyen.platform.home.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read model del Dashboard Home.
 */
public record GetHomeDashboardResult(
        LocalDate fromDate,
        LocalDate toDate,
        Instant generatedAt,
        HomeFinancialSummary financialSummary,
        HomeReceivablesSummary receivables,
        HomeReceivablesSummary completedReceivables,
        HomeCommitmentsSummary commitments,
        HomeInventoryAlertsSummary inventoryAlerts,
        HomePaperRollAlertsSummary paperRollAlerts,
        HomeProductionSummary productionSummary,
        HomeProfitabilitySummary profitabilitySummary
) {
}
