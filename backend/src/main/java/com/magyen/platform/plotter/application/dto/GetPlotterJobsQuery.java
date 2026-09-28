package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Filtro opcional de trabajos de Plotter por fecha de creación, cliente y tipo.
 * <p>
 * Sin fechas se listan todos. Sin customerId no se restringe el cliente.
 * Sin jobType no se restringe el tipo. El filtro de internos no usa customerId.
 * No restringe de forma permanente al mes actual.
 */
public record GetPlotterJobsQuery(
        LocalDate fromDate,
        LocalDate toDate,
        UUID customerId,
        PlotterJobType jobType
) {
    public GetPlotterJobsQuery(LocalDate fromDate, LocalDate toDate) {
        this(fromDate, toDate, null, null);
    }

    public GetPlotterJobsQuery(LocalDate fromDate, LocalDate toDate, UUID customerId) {
        this(fromDate, toDate, customerId, null);
    }
}
