package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderPort;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
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
 * INTERNAL_MAGYEN: operación de material de producción atribuible a una Orden comercial.
 * No crea EXPENSE ni INCOME. El papel se registra una sola vez (sourceId = plotterJobId).
 * EXTERNAL: servicio a cliente; el cobro permanece en el flujo de pagos.
 * <p>
 * Atomicidad: PlotterJob y el OUT comparten transacción. Stock insuficiente no deja estado parcial.
 * Idempotencia: el mismo {@code plotterJobId} no consume papel dos veces.
 */
public class CreatePlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterJobInventoryPort plotterJobInventoryPort;
    private final PlotterCommercialOrderPort plotterCommercialOrderPort;

    public CreatePlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort,
            PlotterCommercialOrderPort plotterCommercialOrderPort
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

        if (jobType.isInternal()) {
            if (orderId == null) {
                throw new PlotterDomainException("Internal Magyen plotter jobs require a commercial order");
            }
            PlotterCommercialOrderView commercialOrder = plotterCommercialOrderPort.requireExistingOrder(orderId);
            customerId = commercialOrder.customerId();
            validateJobDate(creationDate, commercialOrder);
        } else if (orderId != null) {
            throw new PlotterDomainException("External plotter jobs must not reference a commercial order");
        }

        if (customerId == null) {
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
                jobType.isInternal() ? BigDecimal.ZERO : command.pricePerMeter(),
                command.observations()
        );

        PlotterJob savedPlotterJob = plotterJobRepository.save(plotterJob);

        plotterJobInventoryPort.consumePaperMeters(
                savedPlotterJob.getPaperInventoryItemId(),
                savedPlotterJob.getPrintedMeters(),
                savedPlotterJob.getId(),
                savedPlotterJob.getObservations()
        );

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
    }
}
