package com.magyen.platform.plotter.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Corrección exclusiva de la fecha real de trabajo.
 */
public record CorrectPlotterJobDateCommand(
        UUID plotterJobId,
        LocalDate creationDate
) {
}
