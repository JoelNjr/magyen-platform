package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte operativo por máquina para el período solicitado.
 */
public record GetPlotterMachineOperationsResponse(
        LocalDate fromDate,
        LocalDate toDate,
        List<PlotterMachineOperationResponse> machines
) {
}
