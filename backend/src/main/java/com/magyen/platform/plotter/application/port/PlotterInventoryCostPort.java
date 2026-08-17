package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Lectura del snapshot histórico de un OUT de Inventory originado por Plotter.
 */
public interface PlotterInventoryCostPort {

    Optional<PlotterInventoryCostSnapshot> findCostByPlotterJobId(UUID plotterJobId);

    record PlotterInventoryCostSnapshot(
            UUID plotterJobId,
            BigDecimal unitCost,
            BigDecimal totalCost
    ) {
        public boolean valued() {
            return totalCost != null;
        }
    }
}
