package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        return execute(new GetPlotterJobsQuery(null, null));
    }

    public GetPlotterJobsResult execute(GetPlotterJobsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateRange(query.fromDate(), query.toDate());

        List<GetPlotterJobResult> jobs = plotterJobRepository.findAll().stream()
                .filter(job -> inRange(job.getCreationDate(), query.fromDate(), query.toDate()))
                .map(this::toResult)
                .toList();
        return new GetPlotterJobsResult(List.copyOf(jobs));
    }

    private static void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new PlotterDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new PlotterDomainException("From date must not be after to date");
        }
    }

    private static boolean inRange(LocalDate businessDate, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return businessDate != null && !businessDate.isBefore(fromDate) && !businessDate.isAfter(toDate);
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
