package com.magyen.platform.plotter.application.dto;

import java.util.UUID;

/**
 * Consulta de detalle de un trabajo de plotter.
 */
public record GetPlotterJobQuery(UUID plotterJobId) {
}
