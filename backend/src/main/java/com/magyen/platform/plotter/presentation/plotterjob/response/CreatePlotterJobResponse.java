package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP de creación de un trabajo de plotter.
 */
public record CreatePlotterJobResponse(
        UUID plotterJobId,
        String jobType,
        UUID customerId,
        String customerName,
        UUID orderId,
        String orderNumber,
        String orderDescription,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        BigDecimal totalAmount,
        String status,
        String observations
) {
}
