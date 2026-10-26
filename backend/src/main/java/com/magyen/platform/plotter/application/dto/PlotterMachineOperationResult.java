package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Totales operativos de una máquina en un período.
 * <p>
 * {@code printedMeters} suma trabajos internos y externos.
 * {@code wasteMeters} suma solo merma.
 * {@code externalRevenue} suma pagos de trabajos externos, no el total facturado.
 * {@code identified} falso corresponde a trabajos sin máquina.
 */
public record PlotterMachineOperationResult(
        UUID plotterMachineId,
        String machineName,
        boolean identified,
        int jobCount,
        BigDecimal printedMeters,
        BigDecimal wasteMeters,
        BigDecimal externalRevenue
) {
}
