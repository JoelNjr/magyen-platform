package com.magyen.platform.plotter.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de las máquinas de Plotter.
 */
public interface PlotterMachineRepository {

    List<PlotterMachine> findAll();

    Optional<PlotterMachine> findById(UUID id);

    Optional<PlotterMachine> findDefaultActive();

    PlotterMachine save(PlotterMachine plotterMachine);
}
