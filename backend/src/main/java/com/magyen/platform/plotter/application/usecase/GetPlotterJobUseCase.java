package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que obtiene el detalle de un trabajo de plotter.
 */
public class GetPlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public GetPlotterJobUseCase(
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

    public GetPlotterJobResult execute(GetPlotterJobQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.plotterJobId(), "Plotter job id must not be null");

        PlotterJob job = plotterJobRepository.findById(query.plotterJobId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plotter job not found: " + query.plotterJobId()
                ));

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
