package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.PlotterMachineResult;
import com.magyen.platform.plotter.domain.PlotterMachine;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Activa o retira una máquina de los registros nuevos.
 * <p>
 * No reasigna trabajos ni rollos. La referencia histórica permanece.
 */
public class ChangePlotterMachineActiveUseCase {

    private final PlotterMachineRepository plotterMachineRepository;

    public ChangePlotterMachineActiveUseCase(PlotterMachineRepository plotterMachineRepository) {
        this.plotterMachineRepository = Objects.requireNonNull(
                plotterMachineRepository,
                "Plotter machine repository must not be null"
        );
    }

    @Transactional
    public PlotterMachineResult execute(UUID plotterMachineId, boolean active) {
        Objects.requireNonNull(plotterMachineId, "Plotter machine id must not be null");
        PlotterMachine machine = plotterMachineRepository.findById(plotterMachineId)
                .orElseThrow(() -> new PlotterDomainException(
                        "Plotter machine not found: " + plotterMachineId
                ));
        if (active) {
            machine.activate();
        } else {
            machine.deactivate();
        }
        PlotterMachine saved = plotterMachineRepository.save(machine);
        return new PlotterMachineResult(
                saved.getId(),
                saved.getName(),
                saved.isActive(),
                saved.isDefaultForRegistration()
        );
    }
}
