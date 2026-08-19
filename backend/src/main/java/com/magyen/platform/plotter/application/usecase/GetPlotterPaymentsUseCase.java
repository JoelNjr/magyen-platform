package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterPaymentResult;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsResult;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPayment;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Lista pagos de un trabajo de Plotter y el resumen de saldo.
 */
public class GetPlotterPaymentsUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;

    public GetPlotterPaymentsUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
    }

    public GetPlotterPaymentsResult execute(GetPlotterPaymentsQuery query) {
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

        List<GetPlotterPaymentResult> paymentResults = payments.stream()
                .map(payment -> new GetPlotterPaymentResult(
                        payment.getId(),
                        payment.getPlotterJobId(),
                        payment.getAmount(),
                        payment.getPaymentDate(),
                        payment.getObservations()
                ))
                .toList();

        return new GetPlotterPaymentsResult(
                List.copyOf(paymentResults),
                job.getTotalAmount(),
                paidAmount,
                outstandingAmount
        );
    }
}
