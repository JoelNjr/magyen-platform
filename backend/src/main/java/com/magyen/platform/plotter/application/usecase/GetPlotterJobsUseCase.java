package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.domain.PlotterJobRepository;

import java.util.Objects;

/**
 * Caso de uso que lista los trabajos de plotter.
 */
public class GetPlotterJobsUseCase {

    private final PlotterJobRepository plotterJobRepository;

    public GetPlotterJobsUseCase(PlotterJobRepository plotterJobRepository) {
        this.plotterJobRepository = Objects.requireNonNull(
                plotterJobRepository,
                "Plotter job repository must not be null"
        );
    }

    public GetPlotterJobsResult execute() {
        return new GetPlotterJobsResult(
                plotterJobRepository.findAll().stream()
                        .map(PlotterJobReadMapper::toGetResult)
                        .toList()
        );
    }
}
