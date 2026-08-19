package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityResult;
import com.magyen.platform.plotter.application.dto.PlotterInternalOrderCostItem;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.application.port.PlotterInkAcquisitionPort;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort;
import com.magyen.platform.plotter.application.port.PlotterInventoryCostPort.PlotterInventoryCostSnapshot;
import com.magyen.platform.plotter.application.port.PlotterPaperAcquisitionPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lectura analítica de Plotter. No crea INCOME ni EXPENSE.
 * <p>
 * El gasto en papel y tinta es la suma de adquisiciones de Inventario en el período,
 * no de consumos OUT ni de estimaciones por trabajo.
 * La cobranza EXTERNAL suma pagos reales de esos trabajos; no es costo de material.
 */
public class GetPlotterProfitabilityUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_METERS = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterInventoryCostPort plotterInventoryCostPort;
    private final PlotterPaperAcquisitionPort plotterPaperAcquisitionPort;
    private final PlotterInkAcquisitionPort plotterInkAcquisitionPort;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final Clock clock;

    public GetPlotterProfitabilityUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterInventoryCostPort plotterInventoryCostPort,
            PlotterPaperAcquisitionPort plotterPaperAcquisitionPort,
            PlotterInkAcquisitionPort plotterInkAcquisitionPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            Clock clock
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterInventoryCostPort = Objects.requireNonNull(
                plotterInventoryCostPort,
                "Plotter inventory cost port must not be null"
        );
        this.plotterPaperAcquisitionPort = Objects.requireNonNull(
                plotterPaperAcquisitionPort,
                "Plotter paper acquisition port must not be null"
        );
        this.plotterInkAcquisitionPort = Objects.requireNonNull(
                plotterInkAcquisitionPort,
                "Plotter ink acquisition port must not be null"
        );
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetPlotterProfitabilityResult execute(GetPlotterProfitabilityQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        PlotterProfitabilityScope scope = query.scope() == null
                ? PlotterProfitabilityScope.ALL
                : query.scope();
        ResolvedPeriod period = resolvePeriod(query.fromDate(), query.toDate());

        List<PlotterJob> jobs = plotterJobRepository.findAll().stream()
                .filter(job -> !job.getCreationDate().isBefore(period.fromDate()))
                .filter(job -> !job.getCreationDate().isAfter(period.toDate()))
                .filter(job -> matchesScope(job, scope))
                .sorted(Comparator.comparing(PlotterJob::getCreationDate)
                        .thenComparing(job -> job.getId().toString()))
                .toList();

        BigDecimal totalPaperPrintedMeters = ZERO_METERS;
        BigDecimal internalPaperPrintedMeters = ZERO_METERS;
        BigDecimal wastePrintedMeters = ZERO_METERS;
        BigDecimal externalRevenue = ZERO_MONEY;
        BigDecimal internalRevenue = ZERO_MONEY;
        BigDecimal externalPaidAmount = ZERO_MONEY;
        BigDecimal externalOutstandingAmount = ZERO_MONEY;
        int externalJobCount = 0;
        int internalJobCount = 0;
        int wasteJobCount = 0;
        List<PlotterInternalOrderCostItem> internalOrders = new ArrayList<>();

        for (PlotterJob job : jobs) {
            totalPaperPrintedMeters = totalPaperPrintedMeters.add(job.getPrintedMeters());
            PlotterInventoryCostSnapshot snapshot = plotterInventoryCostPort
                    .findCostByPlotterJobId(job.getId())
                    .orElse(null);
            boolean valued = snapshot != null && snapshot.valued();

            if (job.getJobType().isInternal()) {
                internalJobCount++;
                internalPaperPrintedMeters = internalPaperPrintedMeters.add(job.getPrintedMeters());
                internalRevenue = internalRevenue.add(job.getTotalAmount());
                internalOrders.add(toInternalItem(job, snapshot, valued));
            } else if (job.getJobType().isWaste()) {
                wasteJobCount++;
                wastePrintedMeters = wastePrintedMeters.add(job.getPrintedMeters());
            } else {
                externalJobCount++;
                externalRevenue = externalRevenue.add(job.getTotalAmount());
                List<PlotterPayment> payments =
                        plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId());
                BigDecimal paidAmount = PlotterPaymentBalanceCalculator.sumPaid(payments);
                BigDecimal outstandingAmount =
                        PlotterPaymentBalanceCalculator.collectableOutstanding(job, paidAmount);
                externalPaidAmount = externalPaidAmount.add(paidAmount);
                externalOutstandingAmount = externalOutstandingAmount.add(outstandingAmount);
            }
        }

        BigDecimal paperAcquisitionCost = plotterPaperAcquisitionPort
                .findPaperAcquisitions(period.fromDate(), period.toDate())
                .stream()
                .map(acquisition -> acquisition.totalCost() == null ? ZERO_MONEY : acquisition.totalCost())
                .reduce(ZERO_MONEY, BigDecimal::add);
        BigDecimal inkAcquisitionCost = plotterInkAcquisitionPort
                .findInkAcquisitions(period.fromDate(), period.toDate())
                .stream()
                .map(acquisition -> acquisition.totalCost() == null ? ZERO_MONEY : acquisition.totalCost())
                .reduce(ZERO_MONEY, BigDecimal::add);
        BigDecimal combinedRevenue = money(externalRevenue.add(internalRevenue));
        BigDecimal totalPaperCost = money(paperAcquisitionCost);
        BigDecimal inkCost = money(inkAcquisitionCost);
        BigDecimal analyticalPlotterResult = money(combinedRevenue.subtract(totalPaperCost).subtract(inkCost));

        return new GetPlotterProfitabilityResult(
                period.fromDate(),
                period.toDate(),
                scope,
                jobs.size(),
                externalJobCount,
                internalJobCount,
                wasteJobCount,
                totalPaperPrintedMeters.setScale(4, RoundingMode.HALF_UP),
                wastePrintedMeters.setScale(4, RoundingMode.HALF_UP),
                money(externalRevenue),
                money(internalRevenue),
                combinedRevenue,
                ZERO_MONEY,
                ZERO_MONEY,
                totalPaperCost,
                internalPaperPrintedMeters.setScale(4, RoundingMode.HALF_UP),
                0,
                true,
                true,
                inkCost,
                analyticalPlotterResult,
                money(externalPaidAmount),
                money(externalOutstandingAmount),
                List.copyOf(internalOrders)
        );
    }

    private PlotterInternalOrderCostItem toInternalItem(
            PlotterJob job,
            PlotterInventoryCostSnapshot snapshot,
            boolean valued
    ) {
        PlotterCommercialOrderView orderView = job.getOrderId() == null
                ? null
                : plotterCommercialOrderPort.findOrder(job.getOrderId()).orElse(null);
        String customerName = orderView != null
                ? orderView.customerName()
                : plotterCommercialOrderPort.findCustomerName(job.getCustomerId()).orElse(null);

        return new PlotterInternalOrderCostItem(
                job.getId(),
                job.getCreationDate(),
                job.getPrintedMeters(),
                valued ? money(snapshot.totalCost()) : null,
                valued,
                money(job.getTotalAmount()),
                job.getOrderId(),
                orderView == null ? null : orderView.orderNumber(),
                orderView == null ? null : orderView.description(),
                customerName
        );
    }

    private static boolean matchesScope(PlotterJob job, PlotterProfitabilityScope scope) {
        if (scope == PlotterProfitabilityScope.ALL) {
            return true;
        }
        if (scope == PlotterProfitabilityScope.INTERNAL) {
            return job.getJobType() == PlotterJobType.INTERNAL_MAGYEN;
        }
        if (scope == PlotterProfitabilityScope.WASTE) {
            return job.getJobType() == PlotterJobType.WASTE;
        }
        return job.getJobType() == PlotterJobType.EXTERNAL;
    }

    private ResolvedPeriod resolvePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            LocalDate today = LocalDate.now(clock);
            return new ResolvedPeriod(
                    today.withDayOfMonth(1),
                    today.withDayOfMonth(today.lengthOfMonth())
            );
        }
        if (fromDate == null || toDate == null) {
            throw new PlotterDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new PlotterDomainException("From date must not be after to date");
        }
        return new ResolvedPeriod(fromDate, toDate);
    }

    private static BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            return ZERO_MONEY;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private record ResolvedPeriod(LocalDate fromDate, LocalDate toDate) {
    }
}
