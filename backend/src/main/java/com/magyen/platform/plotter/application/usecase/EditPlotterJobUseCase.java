package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.EditPlotterJobCommand;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperRollView;
import com.magyen.platform.plotter.application.usecase.PlotterCustomerAccountCalculator.Position;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterPaymentRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Corrige un trabajo conservando su id, cliente, orden y máquina.
 * <p>
 * Si cambian el rollo o los metros, el consumo de inventario se ajusta una sola vez.
 * Si cambia el valor comercial externo, el débito de la cuenta es el nuevo total del
 * mismo trabajo. Los pagos recibidos no se reescriben.
 */
public class EditPlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterPaymentRepository plotterPaymentRepository;
    private final PlotterJobInventoryPort plotterJobInventoryPort;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final PlotterInternalServiceFinancePort plotterInternalServiceFinancePort;

    public EditPlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterPaymentRepository plotterPaymentRepository,
            PlotterJobInventoryPort plotterJobInventoryPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
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
    public PlotterJob execute(EditPlotterJobCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.plotterJobId(), "Plotter job id must not be null");
        Objects.requireNonNull(command.creationDate(), "Creation date must not be null");
        Objects.requireNonNull(command.printedMeters(), "Printed meters must not be null");

        PlotterJob job = plotterJobRepository.findById(command.plotterJobId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plotter job not found: " + command.plotterJobId()
                ));
        if (command.plotterMachineId() != null
                && !command.plotterMachineId().equals(job.getPlotterMachineId())) {
            throw new PlotterDomainException(
                    "Plotter machine assignment cannot be changed on an existing job"
            );
        }

        UUID paperRollId = command.paperInventoryItemId() == null
                ? job.getPaperInventoryItemId()
                : command.paperInventoryItemId();
        BigDecimal pricePerMeter = job.getJobType().isWaste()
                ? BigDecimal.ZERO
                : command.pricePerMeter();
        BigDecimal commercialValue = job.getJobType().isExternal()
                ? (command.commercialValue() == null ? job.getTotalAmount() : command.commercialValue())
                : null;

        if (job.getJobType().isInternal()) {
            PlotterCommercialOrderView order = plotterCommercialOrderPort.findOrder(job.getOrderId())
                    .orElseThrow(() -> new PlotterDomainException(
                            "Commercial order not found: " + job.getOrderId()
                    ));
            PlotterJobDateRules.validateAgainstOrder(command.creationDate(), order);
        }

        PlotterJob corrected = job.correct(
                command.creationDate(),
                paperRollId,
                command.printedMeters(),
                pricePerMeter,
                commercialValue,
                command.observations()
        );
        rejectExternalValueBelowPayments(job, corrected);

        boolean rollChanged = !paperRollId.equals(job.getPaperInventoryItemId());
        boolean metersChanged = command.printedMeters().compareTo(job.getPrintedMeters()) != 0;
        if (rollChanged || metersChanged) {
            if (job.getPlotterMachineId() == null && rollChanged) {
                throw new PlotterDomainException(
                        "Cannot change the paper roll of a job without a Plotter machine"
                );
            }
            if (job.getPlotterMachineId() != null) {
                PlotterPaperRollView paperRoll = plotterJobInventoryPort.requirePlotterPaperRoll(paperRollId);
                if (!job.getPlotterMachineId().equals(paperRoll.plotterMachineId())) {
                    throw new PlotterDomainException(
                            "Paper roll is assigned to a different Plotter machine"
                    );
                }
            }
            plotterJobInventoryPort.correctPaperConsumption(
                    paperRollId,
                    corrected.getPrintedMeters(),
                    job.getId(),
                    corrected.getObservations()
            );
        }

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

    private void rejectExternalValueBelowPayments(PlotterJob current, PlotterJob corrected) {
        if (!current.getJobType().isExternal()) {
            return;
        }
        Position position = PlotterCustomerAccountCalculator.positionOf(
                current.getCustomerId(),
                plotterJobRepository.findAll(),
                plotterPaymentRepository.findAll()
        );
        BigDecimal nextDebits = position.debitTotal()
                .subtract(current.getTotalAmount())
                .add(corrected.getTotalAmount());
        if (nextDebits.subtract(position.paymentTotal()).compareTo(BigDecimal.ZERO) < 0) {
            throw new PlotterDomainException(
                    "The commercial value cannot be reduced below payments already received on the customer account. Outstanding after the change would be negative."
            );
        }
    }
}
