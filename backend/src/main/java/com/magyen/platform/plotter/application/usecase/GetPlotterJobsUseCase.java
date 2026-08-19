package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que lista los trabajos de plotter.
 */
public class GetPlotterJobsUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public GetPlotterJobsUseCase(
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

    public GetPlotterJobsResult execute() {
        List<GetPlotterJobResult> jobs = plotterJobRepository.findAll().stream()
                .map(this::toResult)
                .toList();
        return new GetPlotterJobsResult(List.copyOf(jobs));
    }

    private GetPlotterJobResult toResult(PlotterJob job) {
        List<PlotterPayment> payments =
                plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId());
        BigDecimal paidAmount = PlotterPaymentBalanceCalculator.sumPaid(payments);
        BigDecimal outstandingAmount =
                PlotterPaymentBalanceCalculator.collectableOutstanding(job, paidAmount);
        return PlotterJobReadMapper.toGetResult(
                job,
                paidAmount,
                outstandingAmount,
                plotterCommercialOrderPort
        );
    }
}
