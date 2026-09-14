package com.magyen.platform.home.application.usecase;

import com.magyen.platform.home.application.HomePeriodResolver;
import com.magyen.platform.home.application.HomePeriodResolver.ResolvedPeriod;
import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.dto.HomeCommitmentItem;
import com.magyen.platform.home.application.dto.HomeCommitmentsSummary;
import com.magyen.platform.home.application.dto.HomeFinancialSummary;
import com.magyen.platform.home.application.dto.HomeInventoryAlertItem;
import com.magyen.platform.home.application.dto.HomeInventoryAlertsSummary;
import com.magyen.platform.home.application.dto.HomePaperRollAlertItem;
import com.magyen.platform.home.application.dto.HomePaperRollAlertsSummary;
import com.magyen.platform.home.application.dto.HomeProductionItem;
import com.magyen.platform.home.application.dto.HomeProductionSummary;
import com.magyen.platform.home.application.dto.HomeProfitabilitySummary;
import com.magyen.platform.home.application.dto.HomeReceivableItem;
import com.magyen.platform.home.application.dto.HomeReceivablesSummary;
import com.magyen.platform.home.application.port.CommercialDashboardPort;
import com.magyen.platform.home.application.port.FinanceDashboardPort;
import com.magyen.platform.home.application.port.InventoryDashboardPort;
import com.magyen.platform.home.application.port.ProductionDashboardPort;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Orquesta el read model del Dashboard Home.
 * <p>
 * Solo lectura: no modifica Finance, Inventory, Production, Plotter ni Commercial.
 * El período resuelto aplica al resumen financiero ({@code transactionDate})
 * y a la rentabilidad comercial (entrega programada). El resto es snapshot actual.
 */
public class GetHomeDashboardUseCase {

    private final FinanceDashboardPort financeDashboardPort;
    private final CommercialDashboardPort commercialDashboardPort;
    private final InventoryDashboardPort inventoryDashboardPort;
    private final ProductionDashboardPort productionDashboardPort;
    private final Clock clock;

    public GetHomeDashboardUseCase(
            FinanceDashboardPort financeDashboardPort,
            CommercialDashboardPort commercialDashboardPort,
            InventoryDashboardPort inventoryDashboardPort,
            ProductionDashboardPort productionDashboardPort,
            Clock clock
    ) {
        this.financeDashboardPort = Objects.requireNonNull(
                financeDashboardPort,
                "Finance dashboard port must not be null"
        );
        this.commercialDashboardPort = Objects.requireNonNull(
                commercialDashboardPort,
                "Commercial dashboard port must not be null"
        );
        this.inventoryDashboardPort = Objects.requireNonNull(
                inventoryDashboardPort,
                "Inventory dashboard port must not be null"
        );
        this.productionDashboardPort = Objects.requireNonNull(
                productionDashboardPort,
                "Production dashboard port must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetHomeDashboardResult execute(GetHomeDashboardQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        ResolvedPeriod period = HomePeriodResolver.resolve(query.fromDate(), query.toDate(), clock);

        FinanceDashboardPort.FinancePeriodSummary summary = financeDashboardPort.getPeriodSummary(
                period.fromDate(),
                period.toDate()
        );
        CommercialDashboardPort.HomeReceivablesSnapshot receivables =
                commercialDashboardPort.getCurrentOutstandingReceivables();
        CommercialDashboardPort.HomeReceivablesSnapshot completedReceivables =
                commercialDashboardPort.getCompletedOutstandingReceivables();
        FinanceDashboardPort.HomeFinancialCommitmentsSnapshot commitments =
                financeDashboardPort.getCurrentFinancialCommitments();
        InventoryDashboardPort.HomeInventoryAlertsSnapshot inventoryAlerts =
                inventoryDashboardPort.getCurrentInventoryAlerts();
        ProductionDashboardPort.HomeProductionSummarySnapshot production =
                productionDashboardPort.getCurrentProductionSummary();
        CommercialDashboardPort.HomeProfitabilitySummarySnapshot profitability =
                commercialDashboardPort.getProfitabilitySummary(period.fromDate(), period.toDate());

        return new GetHomeDashboardResult(
                period.fromDate(),
                period.toDate(),
                Instant.now(clock),
                new HomeFinancialSummary(
                        summary.income(),
                        summary.expense(),
                        summary.netResult(),
                        summary.transactionCount()
                ),
                toReceivablesSummary(receivables),
                toReceivablesSummary(completedReceivables),
                toCommitmentsSummary(commitments),
                toInventoryAlertsSummary(inventoryAlerts.inventoryAlerts()),
                toPaperRollAlertsSummary(inventoryAlerts.paperRollAlerts()),
                toProductionSummary(production),
                toProfitabilitySummary(profitability)
        );
    }

    private static HomeReceivablesSummary toReceivablesSummary(
            CommercialDashboardPort.HomeReceivablesSnapshot snapshot
    ) {
        return new HomeReceivablesSummary(
                snapshot.totalOutstandingAmount(),
                snapshot.totalCollectedAmount(),
                snapshot.orderCount(),
                snapshot.items().stream()
                        .map(item -> new HomeReceivableItem(
                                item.orderId(),
                                item.orderNumber(),
                                item.description(),
                                item.customerId(),
                                item.customerName(),
                                item.orderValue(),
                                item.collectedAmount(),
                                item.outstandingAmount(),
                                item.promisedDeliveryDate()
                        ))
                        .toList()
        );
    }

    private static HomeCommitmentsSummary toCommitmentsSummary(
            FinanceDashboardPort.HomeFinancialCommitmentsSnapshot snapshot
    ) {
        return new HomeCommitmentsSummary(
                snapshot.totalPendingAmount(),
                snapshot.totalOverdueAmount(),
                snapshot.overdueCount(),
                snapshot.upcomingCount(),
                snapshot.items().stream()
                        .map(item -> new HomeCommitmentItem(
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
        );
    }

    private static HomeInventoryAlertsSummary toInventoryAlertsSummary(
            InventoryDashboardPort.InventoryAlertsSection section
    ) {
        return new HomeInventoryAlertsSummary(
                section.lowStockCount(),
                section.items().stream()
                        .map(item -> new HomeInventoryAlertItem(
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
        );
    }

    private static HomePaperRollAlertsSummary toPaperRollAlertsSummary(
            InventoryDashboardPort.PaperRollAlertsSection section
    ) {
        return new HomePaperRollAlertsSummary(
                section.lowStockCount(),
                section.items().stream()
                        .map(item -> new HomePaperRollAlertItem(
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
        );
    }

    private static HomeProductionSummary toProductionSummary(
            ProductionDashboardPort.HomeProductionSummarySnapshot snapshot
    ) {
        return new HomeProductionSummary(
                snapshot.totalOrders(),
                snapshot.createdCount(),
                snapshot.plannedCount(),
                snapshot.inProgressCount(),
                snapshot.completedCount(),
                snapshot.items().stream()
                        .map(item -> new HomeProductionItem(
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
        );
    }

    private static HomeProfitabilitySummary toProfitabilitySummary(
            CommercialDashboardPort.HomeProfitabilitySummarySnapshot snapshot
    ) {
        return new HomeProfitabilitySummary(
                snapshot.evaluatedOrderCount(),
                snapshot.completeOrderCount(),
                snapshot.partiallyUnvaluedOrderCount(),
                snapshot.noCostDataOrderCount(),
                snapshot.totalOrderValue(),
                snapshot.totalDirectCost(),
                snapshot.totalDirectProfit(),
                snapshot.averageMarginPercentage(),
                snapshot.unvaluedCostCount()
        );
    }

}
