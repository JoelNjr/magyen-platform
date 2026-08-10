package com.magyen.platform.plotter.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia del agregado {@link PlotterJob}.
 */
public interface PlotterJobRepository {

    PlotterJob save(PlotterJob plotterJob);

    Optional<PlotterJob> findById(UUID id);

    List<PlotterJob> findAll();
}
