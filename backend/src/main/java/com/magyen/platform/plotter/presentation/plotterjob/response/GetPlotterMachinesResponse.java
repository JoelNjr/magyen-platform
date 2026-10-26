package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.util.List;

/**
 * Listado de máquinas de Plotter.
 */
public record GetPlotterMachinesResponse(List<PlotterMachineResponse> machines) {
}
