package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.time.LocalDate;

/**
 * Corrección exclusiva de la fecha real de trabajo.
 */
public record CorrectPlotterJobDateRequest(
        LocalDate creationDate
) {
}
