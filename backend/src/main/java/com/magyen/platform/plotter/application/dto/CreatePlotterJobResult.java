package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de crear un trabajo de plotter.
 */
public record CreatePlotterJobResult(
        UUID plotterJobId,
        PlotterJobType jobType,
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
        PlotterJobStatus status,
        String observations
) {
}
