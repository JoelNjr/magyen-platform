package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterPendingBalancesResult;
import com.magyen.platform.plotter.application.dto.PlotterCustomerPendingBalance;
import com.magyen.platform.plotter.application.dto.PlotterNegativeBalanceItem;
import com.magyen.platform.plotter.application.dto.PlotterPendingJobBalance;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Lee la deuda de trabajos externos de Plotter. No persiste saldo ni crea
 * movimientos de Finance. No usa el saldo comercial de las órdenes.
 * <p>
 * El cálculo es histórico completo: no aplica el mes del listado de trabajos.
 */
public class GetPlotterPendingBalancesUseCase {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public GetPlotterPendingBalancesUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
    }

    public GetPlotterPendingBalancesResult execute() {
        BigDecimal externalBilled = money(BigDecimal.ZERO);
        BigDecimal externalPaid = money(BigDecimal.ZERO);
        BigDecimal outstanding = money(BigDecimal.ZERO);
        int openJobCount = 0;
        Map<UUID, CustomerAccumulator> customers = new LinkedHashMap<>();
        List<PlotterNegativeBalanceItem> negativeBalances = new ArrayList<>();

        for (PlotterJob job : plotterJobRepository.findAll()) {
            if (!job.getJobType().isExternal() || job.getStatus() == PlotterJobStatus.CANCELLED) {
                continue;
            }
            BigDecimal paidAmount = PlotterPaymentBalanceCalculator.sumPaid(
                    plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId())
            );
            BigDecimal jobOutstanding = PlotterPaymentBalanceCalculator.outstanding(
                    job.getTotalAmount(),
                    paidAmount
            );
            externalBilled = externalBilled.add(money(job.getTotalAmount()));
            externalPaid = externalPaid.add(paidAmount);

            if (jobOutstanding.compareTo(BigDecimal.ZERO) < 0) {
                negativeBalances.add(new PlotterNegativeBalanceItem(
                        job.getId(),
                        job.getCustomerId(),
                        customerName(job.getCustomerId()),
                        job.getCreationDate(),
                        money(job.getTotalAmount()),
                        paidAmount,
                        jobOutstanding
                ));
                continue;
            }
            if (jobOutstanding.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            openJobCount++;
            outstanding = outstanding.add(jobOutstanding);
            customers.computeIfAbsent(job.getCustomerId(), CustomerAccumulator::new)
                    .add(job, paidAmount, jobOutstanding);
        }

        List<PlotterCustomerPendingBalance> customerBalances = customers.values().stream()
                .map(accumulator -> accumulator.toResult(this::customerName))
                .sorted(Comparator
                        .comparing(PlotterCustomerPendingBalance::outstandingAmount).reversed()
                        .thenComparing(balance -> balance.customerName() == null ? "" : balance.customerName()))
                .toList();

        negativeBalances.sort(Comparator.comparing(PlotterNegativeBalanceItem::creationDate));

        return new GetPlotterPendingBalancesResult(
                List.copyOf(customerBalances),
                openJobCount,
                customerBalances.size(),
                externalBilled,
                externalPaid,
                outstanding,
                List.copyOf(negativeBalances)
        );
    }

    private String customerName(UUID customerId) {
        if (customerId == null) {
            return null;
        }
        return plotterCommercialOrderPort.findCustomerName(customerId).orElse(null);
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }

    private static final class CustomerAccumulator {
        private final UUID customerId;
        private BigDecimal billed = money(BigDecimal.ZERO);
        private BigDecimal paid = money(BigDecimal.ZERO);
        private BigDecimal outstanding = money(BigDecimal.ZERO);
        private final List<PlotterPendingJobBalance> jobs = new ArrayList<>();

        private CustomerAccumulator(UUID customerId) {
            this.customerId = customerId;
        }

        private void add(PlotterJob job, BigDecimal paidAmount, BigDecimal jobOutstanding) {
            billed = billed.add(money(job.getTotalAmount()));
            paid = paid.add(paidAmount);
            outstanding = outstanding.add(jobOutstanding);
            jobs.add(new PlotterPendingJobBalance(
                    job.getId(),
                    job.getCreationDate(),
                    money(job.getTotalAmount()),
                    paidAmount,
                    jobOutstanding,
                    job.getStatus()
            ));
        }

        private PlotterCustomerPendingBalance toResult(java.util.function.Function<UUID, String> names) {
            List<PlotterPendingJobBalance> orderedJobs = jobs.stream()
                    .sorted(Comparator
                            .comparing(PlotterPendingJobBalance::creationDate)
                            .thenComparing(job -> job.plotterJobId().toString()))
                    .toList();
            return new PlotterCustomerPendingBalance(
                    customerId,
                    names.apply(customerId),
                    orderedJobs.size(),
                    billed,
                    paid,
                    outstanding,
                    orderedJobs
            );
        }
    }
}
