package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Totales operativos de una máquina. {@code machineName} es "Sin identificar" cuando no hay máquina.
 */
public record PlotterMachineOperationResponse(
        UUID plotterMachineId,
        String machineName,
        boolean identified,
        int jobCount,
        BigDecimal printedMeters,
        BigDecimal wasteMeters,
        BigDecimal externalRevenue
) {
}
