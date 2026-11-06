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

    List<PlotterJob> findByOrderId(UUID orderId);

    List<PlotterJob> findAll();

    /**
     * Borra un trabajo sin evidencia de producción, inventario ni dinero.
     */
    void deleteById(UUID id);
}
