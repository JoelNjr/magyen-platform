package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;

import java.util.UUID;

/**
 * Resultado de anular o borrar un trabajo registrado por error.
 */
public record CancelPlotterJobResult(
        UUID plotterJobId,
        boolean deleted,
        boolean alreadyCancelled,
        PlotterJobStatus status
) {
}
