package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CancelPlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.usecase.PlotterCustomerAccountCalculator.Position;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Quita un trabajo registrado por error sin borrar evidencia de producción o de dinero.
 * <p>
 * Si no hay consumo, pagos ni asientos, la fila se elimina. En cualquier otro caso
 * el trabajo queda {@link PlotterJobStatus#CANCELLED}, el papel se devuelve una vez
 * y, si es interno, se retiran los dos lados del par contable. Los pagos recibidos
 * permanecen en la cuenta y en Finanzas.
 */
public class CancelPlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterJobInventoryPort plotterJobInventoryPort;
    private final PlotterInternalServiceFinancePort plotterInternalServiceFinancePort;

    public CancelPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterJobInventoryPort plotterJobInventoryPort,
            PlotterInternalServiceFinancePort plotterInternalServiceFinancePort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(plotterJobRepository, "Plotter job repository must not be null");
        this.plotterPaymentRepository = Objects.requireNonNull(
                plotterPaymentRepository,
                "Plotter payment repository must not be null"
        );
        this.plotterJobInventoryPort = Objects.requireNonNull(
                plotterJobInventoryPort,
                "Plotter job inventory port must not be null"
        );
        this.plotterInternalServiceFinancePort = Objects.requireNonNull(
                plotterInternalServiceFinancePort,
                "Plotter internal service finance port must not be null"
        );
    }

    @Transactional
    public CancelPlotterJobResult execute(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        PlotterJob job = plotterJobRepository.findById(plotterJobId)
                .orElseThrow(() -> new IllegalArgumentException("Plotter job not found: " + plotterJobId));
        if (job.isCancelled()) {
            return new CancelPlotterJobResult(job.getId(), false, true, PlotterJobStatus.CANCELLED);
        }

        if (job.getJobType().isExternal()) {
            Position position = PlotterCustomerAccountCalculator.positionOf(
                    job.getCustomerId(),
                    plotterJobRepository.findAll(),
                    plotterPaymentRepository.findAll()
            );
            BigDecimal after = position.debitTotal()
                    .subtract(job.getTotalAmount())
                    .subtract(position.paymentTotal());
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                throw new PlotterDomainException(
                        "Cancelling this external plotter job would leave customer payments greater than the remaining job debits. Reduce or reassign the account only after the remaining work covers the money already received."
                );
            }
        }

        boolean hasConsumption = plotterJobInventoryPort.hasPaperConsumption(job.getId());
        boolean hasPayments = !plotterPaymentRepository.findByPlotterJobIdNewestFirst(job.getId()).isEmpty();
        boolean hasLedger = plotterInternalServiceFinancePort.hasInternalServiceLedger(job.getId());
        if (!hasConsumption && !hasPayments && !hasLedger) {
            plotterJobRepository.deleteById(job.getId());
            return new CancelPlotterJobResult(job.getId(), true, false, null);
        }

        if (hasConsumption) {
            plotterJobInventoryPort.reversePaperConsumption(job.getId());
        }
        if (job.getJobType().isInternal()) {
            plotterInternalServiceFinancePort.reverseInternalServiceLedger(job.getId());
        }
        PlotterJob cancelled = plotterJobRepository.save(job.cancel());
        return new CancelPlotterJobResult(cancelled.getId(), false, false, cancelled.getStatus());
    }
}
