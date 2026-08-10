package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.domain.PlotterJobRepository;

import java.util.Objects;

/**
 * Caso de uso que obtiene el detalle de un trabajo de plotter.
 */
public class GetPlotterJobUseCase {

    private final PlotterJobRepository plotterJobRepository;

    public GetPlotterJobUseCase(PlotterJobRepository plotterJobRepository) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
    }

    public GetPlotterJobResult execute(GetPlotterJobQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.plotterJobId(), "Plotter job id must not be null");

        return plotterJobRepository.findById(query.plotterJobId())
                .map(PlotterJobReadMapper::toGetResult)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Plotter job not found: " + query.plotterJobId()
                ));
    }
}
