package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un trabajo de plotter.
 * <p>
 * {@code plotterJobId} es opcional: el mismo identificador no consume papel dos veces.
 * {@code jobType} nulo se resuelve como {@code EXTERNAL} si no hay orden, o
 * {@code INTERNAL_MAGYEN} si hay orden.
 */
public record CreatePlotterJobCommand(
        UUID customerId,
        UUID orderId,
        LocalDate creationDate,
        UUID paperInventoryItemId,
        BigDecimal printedMeters,
        BigDecimal pricePerMeter,
        String observations,
        PlotterJobType jobType,
        UUID plotterJobId
) {
    public CreatePlotterJobCommand(
            UUID customerId,
            UUID orderId,
            LocalDate creationDate,
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            BigDecimal pricePerMeter,
            String observations
    ) {
        this(
                customerId,
                orderId,
                creationDate,
                paperInventoryItemId,
                printedMeters,
                pricePerMeter,
                observations,
                null,
                null
        );
    }
}
