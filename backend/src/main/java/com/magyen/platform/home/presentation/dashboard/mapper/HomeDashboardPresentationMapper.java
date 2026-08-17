package com.magyen.platform.home.presentation.dashboard.mapper;

import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.dto.HomeCommitmentsSummary;
import com.magyen.platform.home.application.dto.HomeFinancialSummary;
import com.magyen.platform.home.application.dto.HomeInventoryAlertsSummary;
import com.magyen.platform.home.application.dto.HomePaperRollAlertsSummary;
import com.magyen.platform.home.application.dto.HomeProductionSummary;
import com.magyen.platform.home.application.dto.HomeProfitabilitySummary;
import com.magyen.platform.home.application.dto.HomeReceivableItem;
import com.magyen.platform.home.application.dto.HomeReceivablesSummary;
import com.magyen.platform.home.presentation.dashboard.response.HomeCommitmentItemResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeCommitmentsResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeDashboardResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeFinancialSummaryResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeInventoryAlertItemResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeInventoryAlertsResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomePaperRollAlertItemResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomePaperRollAlertsResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeProductionItemResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeProductionSummaryResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeProfitabilitySummaryResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeReceivableItemResponse;
import com.magyen.platform.home.presentation.dashboard.response.HomeReceivablesResponse;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Convierte entre HTTP y Application para el Dashboard Home.
 */
public class HomeDashboardPresentationMapper {

    public GetHomeDashboardQuery toQuery(LocalDate fromDate, LocalDate toDate) {
        return new GetHomeDashboardQuery(fromDate, toDate);
    }

    public HomeDashboardResponse toResponse(GetHomeDashboardResult result) {
        Objects.requireNonNull(result, "Dashboard result must not be null");
        HomeFinancialSummary summary = result.financialSummary();
        HomeReceivablesSummary receivables = result.receivables();
        HomeReceivablesSummary completedReceivables = result.completedReceivables();
        HomeCommitmentsSummary commitments = result.commitments();
        HomeInventoryAlertsSummary inventoryAlerts = result.inventoryAlerts();
        HomePaperRollAlertsSummary paperRollAlerts = result.paperRollAlerts();
        HomeProductionSummary productionSummary = result.productionSummary();
        HomeProfitabilitySummary profitabilitySummary = result.profitabilitySummary();
        return new HomeDashboardResponse(
                result.fromDate(),
                result.toDate(),
                result.generatedAt(),
                new HomeFinancialSummaryResponse(
                        summary.income(),
                        summary.expense(),
                        summary.netResult(),
                        summary.transactionCount()
                ),
                toReceivablesResponse(receivables),
                toReceivablesResponse(completedReceivables),
                new HomeCommitmentsResponse(
                        commitments.totalPendingAmount(),
                        commitments.totalOverdueAmount(),
                        commitments.overdueCount(),
                        commitments.upcomingCount(),
                        commitments.items().stream()
                                .map(item -> new HomeCommitmentItemResponse(
                                        item.occurrenceId(),
                                        item.obligationId(),
                                        item.name(),
                                        item.type(),
                                        item.expectedAmount(),
                                        item.dueDate(),
                                        item.status(),
                                        item.overdue(),
                                        item.daysUntilDue(),
                                        item.daysOverdue()
                                ))
                                .toList()
                ),
                new HomeInventoryAlertsResponse(
                        inventoryAlerts.lowStockCount(),
                        inventoryAlerts.items().stream()
                                .map(item -> new HomeInventoryAlertItemResponse(
                                        item.inventoryItemId(),
                                        item.materialCode(),
                                        item.name(),
                                        item.description(),
                                        item.materialType(),
                                        item.paperRollNumber(),
                                        item.stock(),
                                        item.unitOfMeasure(),
                                        item.minimumStock(),
                                        item.lowStock()
                                ))
                                .toList()
                ),
                new HomePaperRollAlertsResponse(
                        paperRollAlerts.lowStockCount(),
                        paperRollAlerts.items().stream()
                                .map(item -> new HomePaperRollAlertItemResponse(
                                        item.inventoryItemId(),
                                        item.materialCode(),
                                        item.name(),
                                        item.paperRollNumber(),
                                        item.stock(),
                                        item.unitOfMeasure(),
                                        item.minimumStock(),
                                        item.lowStock()
                                ))
                                .toList()
                ),
                new HomeProductionSummaryResponse(
                        productionSummary.totalOrders(),
                        productionSummary.createdCount(),
                        productionSummary.plannedCount(),
                        productionSummary.inProgressCount(),
                        productionSummary.completedCount(),
                        productionSummary.items().stream()
                                .map(item -> new HomeProductionItemResponse(
                                        item.productionOrderId(),
                                        item.orderId(),
                                        item.orderNumber(),
                                        item.orderDescription(),
                                        item.customerId(),
                                        item.customerName(),
                                        item.status(),
                                        item.creationDate(),
                                        item.priority()
                                ))
                                .toList()
                ),
                new HomeProfitabilitySummaryResponse(
                        profitabilitySummary.evaluatedOrderCount(),
                        profitabilitySummary.completeOrderCount(),
                        profitabilitySummary.partiallyUnvaluedOrderCount(),
                        profitabilitySummary.noCostDataOrderCount(),
                        profitabilitySummary.totalOrderValue(),
                        profitabilitySummary.totalDirectCost(),
                        profitabilitySummary.totalDirectProfit(),
                        profitabilitySummary.averageMarginPercentage(),
                        profitabilitySummary.unvaluedCostCount()
                )
        );
    }

    private static HomeReceivablesResponse toReceivablesResponse(HomeReceivablesSummary receivables) {
        return new HomeReceivablesResponse(
                receivables.totalOutstandingAmount(),
                receivables.totalCollectedAmount(),
                receivables.orderCount(),
                receivables.items().stream()
                        .map(HomeDashboardPresentationMapper::toReceivableItemResponse)
                        .toList()
        );
    }

    private static HomeReceivableItemResponse toReceivableItemResponse(HomeReceivableItem item) {
        return new HomeReceivableItemResponse(
                item.orderId(),
                item.orderNumber(),
                item.description(),
                item.customerId(),
                item.customerName(),
                item.orderValue(),
                item.collectedAmount(),
                item.outstandingAmount()
        );
    }
}
