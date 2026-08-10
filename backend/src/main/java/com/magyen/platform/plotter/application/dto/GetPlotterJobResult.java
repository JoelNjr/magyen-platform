package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de consulta de un trabajo de plotter.
 */
public record GetPlotterJobResult(
        UUID plotterJobId,
        UUID customerId,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        BigDecimal totalAmount,
        PlotterJobStatus status,
        String observations
) {
}
