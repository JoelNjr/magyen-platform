package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.PlotterMachineResult;
import com.magyen.platform.plotter.domain.PlotterMachineRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lista las máquinas de Plotter, activas e inactivas, para selección e historia.
 */
public class GetPlotterMachinesUseCase {

    private final PlotterMachineRepository plotterMachineRepository;

    public GetPlotterMachinesUseCase(PlotterMachineRepository plotterMachineRepository) {
        this.plotterMachineRepository = Objects.requireNonNull(
                plotterMachineRepository,
                "Plotter machine repository must not be null"
        );
    }

    public List<PlotterMachineResult> execute() {
        return plotterMachineRepository.findAll().stream()
                .sorted(Comparator.comparing(machine -> machine.getName()))
                .map(machine -> new PlotterMachineResult(
                        machine.getId(),
                        machine.getName(),
                        machine.isActive(),
                        machine.isDefaultForRegistration()
                ))
                .toList();
    }
}
