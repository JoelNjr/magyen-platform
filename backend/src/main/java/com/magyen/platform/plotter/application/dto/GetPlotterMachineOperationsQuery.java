package com.magyen.platform.plotter.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Período y máquina opcional del reporte operativo.
 * <p>
 * Metros y cantidad usan la fecha del trabajo. El ingreso externo usa la fecha del pago.
 */
public record GetPlotterMachineOperationsQuery(
        LocalDate fromDate,
        LocalDate toDate,
        UUID plotterMachineId,
        boolean unassignedMachineOnly
) {
}
