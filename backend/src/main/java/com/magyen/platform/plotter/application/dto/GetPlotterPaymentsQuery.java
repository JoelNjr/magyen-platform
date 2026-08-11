package com.magyen.platform.plotter.application.dto;

import java.util.UUID;

/**
 * Consulta de pagos de un trabajo de Plotter.
 */
public record GetPlotterPaymentsQuery(
        UUID plotterJobId
) {
}
