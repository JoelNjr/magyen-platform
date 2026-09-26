package com.magyen.platform.plotter.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtro opcional de trabajos de Plotter por fecha de creación y cliente.
 * <p>
 * Sin fechas se listan todos. Sin customerId no se restringe el cliente.
 * No restringe de forma permanente al mes actual.
 */
public record GetPlotterJobsQuery(
        LocalDate fromDate,
        LocalDate toDate,
        UUID customerId
) {
    public GetPlotterJobsQuery(LocalDate fromDate, LocalDate toDate) {
        this(fromDate, toDate, null);
    }
}
