package com.magyen.platform.commercial.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port de lectura de costo de papel de Plotter interno atribuible a una Orden.
 */
public interface PlotterOrderCostPort {

    PlotterOrderCostSnapshot findCostsByOrderId(UUID orderId);

    record PlotterOrderCostSnapshot(
            BigDecimal plotterMaterialCost,
            int internalJobCount,
            int valuedJobCount,
            int unvaluedJobCount,
            boolean plotterCostAttributable,
            BigDecimal internalPlotterServiceCost,
            BigDecimal attributablePlotterCost
    ) {
    }
}
