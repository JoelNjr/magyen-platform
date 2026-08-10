package com.magyen.platform.plotter.application.dto;

import java.util.List;

/**
 * Resultado de listado de trabajos de plotter.
 */
public record GetPlotterJobsResult(List<GetPlotterJobResult> jobs) {
}
