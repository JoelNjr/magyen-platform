package com.magyen.platform.commercial.infrastructure.plotter;

import com.magyen.platform.commercial.application.port.PlotterOrderCostPort;
import com.magyen.platform.plotter.application.dto.GetInternalPlotterOrderCostsQuery;
import com.magyen.platform.plotter.application.dto.GetInternalPlotterOrderCostsResult;
import com.magyen.platform.plotter.application.usecase.GetInternalPlotterOrderCostsUseCase;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Commercial → Plotter para el costo histórico de papel interno.
 */
public class PlotterOrderCostAdapter implements PlotterOrderCostPort {

    private final GetInternalPlotterOrderCostsUseCase getInternalPlotterOrderCostsUseCase;

    public PlotterOrderCostAdapter(GetInternalPlotterOrderCostsUseCase getInternalPlotterOrderCostsUseCase) {
        this.getInternalPlotterOrderCostsUseCase = Objects.requireNonNull(
                getInternalPlotterOrderCostsUseCase,
                "Get internal plotter order costs use case must not be null"
        );
    }

    @Override
    public PlotterOrderCostSnapshot findCostsByOrderId(UUID orderId) {
        GetInternalPlotterOrderCostsResult result = getInternalPlotterOrderCostsUseCase.execute(
                new GetInternalPlotterOrderCostsQuery(orderId)
        );
        return new PlotterOrderCostSnapshot(
                result.plotterMaterialCost(),
                result.internalJobCount(),
                result.valuedJobCount(),
                result.unvaluedJobCount(),
                result.plotterCostAttributable(),
                result.internalPlotterServiceCost(),
                result.attributablePlotterCost()
        );
    }
}
