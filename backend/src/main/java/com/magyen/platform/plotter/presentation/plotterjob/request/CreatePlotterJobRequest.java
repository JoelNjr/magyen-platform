package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cuerpo HTTP para registrar un trabajo de plotter.
 */
public record CreatePlotterJobRequest(
        String jobType,
        UUID customerId,
        UUID orderId,
        UUID plotterJobId,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        String observations
) {
}
