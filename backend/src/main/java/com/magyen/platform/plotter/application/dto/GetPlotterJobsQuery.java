package com.magyen.platform.plotter.application.dto;

import java.time.LocalDate;

/**
 * Filtro opcional de trabajos de Plotter por fecha de creación.
 * <p>
 * Sin fechas se listan todos. No restringe de forma permanente al mes actual.
 */
public record GetPlotterJobsQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
