package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cuerpo HTTP para registrar un trabajo de plotter.
 */
public record CreatePlotterJobRequest(
        UUID customerId,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        String observations
) {
}
