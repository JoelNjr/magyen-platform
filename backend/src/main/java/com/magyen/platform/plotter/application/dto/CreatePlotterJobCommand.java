package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un trabajo de plotter.
 */
public record CreatePlotterJobCommand(
        UUID customerId,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        String observations
) {
}
