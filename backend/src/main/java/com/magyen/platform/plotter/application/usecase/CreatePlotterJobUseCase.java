package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.application.port.PlotterInternalServiceFinancePort;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperRollView;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Registra un trabajo de Plotter y consume exactamente un OUT de Inventory.
 * <p>
 * INTERNAL_MAGYEN: servicio interno atribuible a una Orden comercial.
 * Crea un par Finance EXPENSE+INCOME del valor del servicio (neto 0).
 * No crea un segundo gasto de compra de papel.
 * EXTERNAL: servicio a cliente; el cobro permanece en el flujo de pagos.
 * WASTE: merma operativa. Consume papel, no exige cliente ni orden, no crea INCOME
 * ni un segundo EXPENSE.
 * <p>
 * Atomicidad: PlotterJob, el OUT y los asientos internos comparten transacción.
 * Idempotencia: el mismo {@code plotterJobId} no consume papel ni duplica asientos.
 */
public class CreatePlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterJobInventoryPort plotterJobInventoryPort;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;
    private final PlotterInternalServiceFinancePort plotterInternalServiceFinancePort;

    public CreatePlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort,
            PlotterInternalServiceFinancePort plotterInternalServiceFinancePort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
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
    public CreatePlotterJobResult execute(CreatePlotterJobCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        if (command.plotterJobId() != null) {
            var existing = plotterJobRepository.findById(command.plotterJobId());
            if (existing.isPresent()) {
                plotterJobInventoryPort.consumePaperMeters(
                        existing.get().getPaperInventoryItemId(),
                        existing.get().getPrintedMeters(),
                        existing.get().getId(),
                        existing.get().getObservations()
                );
                ensureInternalServiceLedger(existing.get());
                return PlotterJobReadMapper.toCreateResult(existing.get(), plotterCommercialOrderPort);
            }
        }

        PlotterPaperRollView paperRoll = plotterJobInventoryPort.requirePlotterPaperRoll(
                command.paperInventoryItemId()
        );

        if (command.printedMeters().compareTo(paperRoll.availableMeters()) > 0) {
            throw new PlotterDomainException(
                    "Printed meters exceed available paper roll stock. Available: "
                            + paperRoll.availableMeters()
                            + ", requested: " + command.printedMeters()
            );
        }

        LocalDate creationDate = command.creationDate() != null
                ? command.creationDate()
                : LocalDate.now();

        PlotterJobType jobType = resolveJobType(command);
        UUID customerId = command.customerId();
        UUID orderId = command.orderId();
        BigDecimal pricePerMeter = command.pricePerMeter();

        if (jobType.isWaste()) {
            if (customerId != null) {
                throw new PlotterDomainException("Waste plotter jobs must not reference a customer");
            }
            if (orderId != null) {
                throw new PlotterDomainException("Waste plotter jobs must not reference a commercial order");
            }
            pricePerMeter = BigDecimal.ZERO;
        } else if (jobType.isInternal()) {
            if (orderId == null) {
                throw new PlotterDomainException("Internal Magyen plotter jobs require a commercial order");
            }
            PlotterCommercialOrderView commercialOrder = plotterCommercialOrderPort.requireExistingOrder(orderId);
            customerId = commercialOrder.customerId();
            validateJobDate(creationDate, commercialOrder);
        } else if (orderId != null) {
            throw new PlotterDomainException("External plotter jobs must not reference a commercial order");
        }

        if (!jobType.isWaste() && customerId == null) {
            throw new PlotterDomainException("Customer id must not be null");
        }

        PlotterJob plotterJob = PlotterJob.create(
                command.plotterJobId(),
                jobType,
                customerId,
                orderId,
                creationDate,
                command.paperInventoryItemId(),
                command.printedMeters(),
                pricePerMeter,
                command.observations()
        );

        PlotterJob savedPlotterJob = plotterJobRepository.save(plotterJob);

        plotterJobInventoryPort.consumePaperMeters(
                savedPlotterJob.getPaperInventoryItemId(),
                savedPlotterJob.getPrintedMeters(),
                savedPlotterJob.getId(),
                savedPlotterJob.getObservations()
        );
        ensureInternalServiceLedger(savedPlotterJob);

        return PlotterJobReadMapper.toCreateResult(savedPlotterJob, plotterCommercialOrderPort);
    }

    private static PlotterJobType resolveJobType(CreatePlotterJobCommand command) {
        if (command.jobType() != null) {
            return command.jobType();
        }
        return command.orderId() == null ? PlotterJobType.EXTERNAL : PlotterJobType.INTERNAL_MAGYEN;
    }

    private static void validateJobDate(LocalDate creationDate, PlotterCommercialOrderView commercialOrder) {
        if (commercialOrder.confirmationDate() != null
                && creationDate.isBefore(commercialOrder.confirmationDate())) {
            throw new PlotterDomainException(
                    "Plotter job date must not be before order confirmation date"
            );
        }
        if (commercialOrder.deliveryDate() != null
                && creationDate.isAfter(commercialOrder.deliveryDate())) {
            throw new PlotterDomainException(
                    "Plotter job date must not be after delivery date"
            );
        }
    }

    private void validateCommand(CreatePlotterJobCommand command) {
        if (command.paperInventoryItemId() == null) {
            throw new PlotterDomainException("Paper inventory item id must not be null");
        }
        if (command.printedMeters() == null) {
            throw new PlotterDomainException("Printed meters must not be null");
        }
        if (command.printedMeters().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlotterDomainException("Printed meters must be greater than zero");
        }
        PlotterJobType jobType = resolveJobType(command);
        if (jobType.isWaste()) {
            return;
        }
        if (jobType.isExternal()) {
            if (command.customerId() == null) {
                throw new PlotterDomainException("Customer id must not be null");
            }
            if (command.pricePerMeter() == null) {
                throw new PlotterDomainException("Price per meter must not be null");
            }
            if (command.pricePerMeter().compareTo(BigDecimal.ZERO) < 0) {
                throw new PlotterDomainException("Price per meter must not be negative");
            }
        }
        if (jobType.isInternal()) {
            if (command.pricePerMeter() == null) {
                throw new PlotterDomainException("Price per meter must not be null");
            }
            if (command.pricePerMeter().compareTo(BigDecimal.ZERO) <= 0) {
                throw new PlotterDomainException("Internal Plotter price per meter must be greater than zero");
            }
        }
    }

    private void ensureInternalServiceLedger(PlotterJob plotterJob) {
        if (!plotterJob.getJobType().isInternal()) {
            return;
        }
        if (plotterJob.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        plotterInternalServiceFinancePort.ensureInternalServiceLedger(
                plotterJob.getId(),
                plotterJob.getTotalAmount(),
                plotterJob.getCreationDate(),
                plotterJob.getObservations()
        );
    }
}
