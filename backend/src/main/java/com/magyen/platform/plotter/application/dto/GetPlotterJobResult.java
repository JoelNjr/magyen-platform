package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;
import com.magyen.platform.plotter.domain.PlotterJobType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de consulta de un trabajo de plotter.
 */
public record GetPlotterJobResult(
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
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        PlotterJobStatus status,
        String observations
) {
}
