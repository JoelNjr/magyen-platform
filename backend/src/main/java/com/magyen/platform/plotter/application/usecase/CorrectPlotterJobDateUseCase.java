package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CorrectPlotterJobDateCommand;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Corrige solo la fecha real de trabajo.
 * <p>
 * No crea otro consumo de papel ni otro débito. La cuenta muestra la fecha nueva
 * porque el débito es el propio trabajo. La fecha de los pagos no cambia.
 * En un trabajo interno actualiza la fecha de los dos asientos, sin crear otros.
 */
public class CorrectPlotterJobDateUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final PlotterInternalServiceFinancePort plotterInternalServiceFinancePort;

    public CorrectPlotterJobDateUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            PlotterInternalServiceFinancePort plotterInternalServiceFinancePort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(plotterJobRepository, "Plotter job repository must not be null");
        this.plotterCommercialOrderPort = Objects.requireNonNull(
                plotterCommercialOrderPort,
                "Plotter commercial order port must not be null"
        );
        this.plotterInternalServiceFinancePort = Objects.requireNonNull(
                plotterInternalServiceFinancePort,
                "Plotter internal service finance port must not be null"
        );
    }

    @Transactional
    public PlotterJob execute(CorrectPlotterJobDateCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterJobId(), "Plotter job id must not be null");
        Objects.requireNonNull(command.creationDate(), "Creation date must not be null");

        PlotterJob job = plotterJobRepository.findById(command.plotterJobId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plotter job not found: " + command.plotterJobId()
                ));
        if (job.isCancelled()) {
            throw new PlotterDomainException("Cancelled plotter jobs cannot be corrected");
        }
        if (command.creationDate().equals(job.getCreationDate())) {
            return job;
        }
        if (job.getJobType().isInternal()) {
            PlotterCommercialOrderView order = plotterCommercialOrderPort.findOrder(job.getOrderId())
                    .orElseThrow(() -> new PlotterDomainException(
                            "Commercial order not found: " + job.getOrderId()
                    ));
            PlotterJobDateRules.validateAgainstOrder(command.creationDate(), order);
        }

        PlotterJob corrected = job.correct(
                command.creationDate(),
                job.getPaperInventoryItemId(),
                job.getPrintedMeters(),
                job.getPricePerMeter(),
                job.getJobType().isExternal() ? job.getTotalAmount() : null,
                job.getObservations()
        );
        PlotterJob saved = plotterJobRepository.save(corrected);
        if (saved.getJobType().isInternal()) {
            plotterInternalServiceFinancePort.synchronizeInternalServiceLedger(
                    saved.getId(),
                    saved.getTotalAmount(),
                    saved.getCreationDate(),
                    saved.getObservations()
            );
        }
        return saved;
    }
}
