package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de crear un trabajo de plotter.
 */
public record CreatePlotterJobResult(
        UUID plotterJobId,
        UUID customerId,
        UUID orderId,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        BigDecimal totalAmount,
        PlotterJobStatus status,
        String observations
) {
}
