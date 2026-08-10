package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.List;

/**
 * Respuesta HTTP de listado de trabajos de plotter.
 */
public record GetPlotterJobsResponse(List<GetPlotterJobResponse> jobs) {
}
