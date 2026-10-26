package com.magyen.platform.plotter.infrastructure.inventory;

import com.magyen.platform.inventory.application.port.PaperRollMachinePort;
import com.magyen.platform.plotter.domain.PlotterMachine;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Resuelve máquinas activas para la recepción y la asignación de rollos.
 */
public class PaperRollMachineAdapter implements PaperRollMachinePort {

    private final PlotterMachineRepository plotterMachineRepository;

    public PaperRollMachineAdapter(PlotterMachineRepository plotterMachineRepository) {
        this.plotterMachineRepository = Objects.requireNonNull(
                plotterMachineRepository,
                "Plotter machine repository must not be null"
        );
    }

    @Override
    public UUID requireActiveMachine(UUID machineId) {
        if (machineId == null) {
            throw new PlotterDomainException("Plotter machine must not be null");
        }
        PlotterMachine machine = plotterMachineRepository.findById(machineId)
                .orElseThrow(() -> new PlotterDomainException("Plotter machine not found: " + machineId));
        if (!machine.isActive()) {
            throw new PlotterDomainException("Plotter machine is inactive: " + machineId);
        }
        return machine.getId();
    }

    @Override
    public UUID requireDefaultActiveMachine() {
        return plotterMachineRepository.findDefaultActive()
                .map(PlotterMachine::getId)
                .orElseThrow(() -> new PlotterDomainException(
                        "Select an active Plotter machine"
                ));
    }
}
