package com.magyen.platform.plotter.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte operativo por máquina. No altera la rentabilidad consolidada.
 */
public record GetPlotterMachineOperationsResult(
        LocalDate fromDate,
        LocalDate toDate,
        List<PlotterMachineOperationResult> machines
) {
}
