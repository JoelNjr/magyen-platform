package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperRollView;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que registra un trabajo de plotter, calcula el ingreso cobrado
 * y consume los metros impresos del rollo de papel seleccionado en Inventory.
 * <p>
 * Atomicidad: PlotterJob y el OUT de Inventory comparten la misma transacción.
 * Si Inventory falla, el trabajo no queda persistido.
 * <p>
 * Idempotencia de stock: Inventory usa {@code sourceId = plotterJobId}.
 */
public class CreatePlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;
    private final PlotterJobInventoryPort plotterJobInventoryPort;

    public CreatePlotterJobUseCase(
            PlotterJobRepository plotterJobRepository,
            PlotterJobInventoryPort plotterJobInventoryPort
    ) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
        this.plotterJobInventoryPort = Objects.requireNonNull(
                plotterJobInventoryPort,
                "Plotter job inventory port must not be null"
        );
    }

    @Transactional
    public CreatePlotterJobResult execute(CreatePlotterJobCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

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

        PlotterJob plotterJob = PlotterJob.create(
                command.customerId(),
                LocalDate.now(),
                command.paperInventoryItemId(),
                command.printedMeters(),
                command.pricePerMeter(),
                command.observations()
        );

        PlotterJob savedPlotterJob = plotterJobRepository.save(plotterJob);

        plotterJobInventoryPort.consumePaperMeters(
                savedPlotterJob.getPaperInventoryItemId(),
                savedPlotterJob.getPrintedMeters(),
                savedPlotterJob.getId(),
                savedPlotterJob.getObservations()
        );

        return PlotterJobReadMapper.toCreateResult(savedPlotterJob);
    }

    private void validateCommand(CreatePlotterJobCommand command) {
        if (command.customerId() == null) {
            throw new PlotterDomainException("Customer id must not be null");
        }
        if (command.paperInventoryItemId() == null) {
            throw new PlotterDomainException("Paper inventory item id must not be null");
        }
        if (command.printedMeters() == null) {
            throw new PlotterDomainException("Printed meters must not be null");
        }
        if (command.printedMeters().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlotterDomainException("Printed meters must be greater than zero");
        }
        if (command.pricePerMeter() == null) {
            throw new PlotterDomainException("Price per meter must not be null");
        }
        if (command.pricePerMeter().compareTo(BigDecimal.ZERO) < 0) {
            throw new PlotterDomainException("Price per meter must not be negative");
        }
    }
}
