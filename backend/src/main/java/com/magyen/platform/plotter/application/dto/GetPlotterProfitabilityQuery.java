package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;

import java.time.LocalDate;

/**
 * Consulta analítica de Plotter. Fechas nulas = mes calendario actual.
 */
public record GetPlotterProfitabilityQuery(
        LocalDate fromDate,
        LocalDate toDate,
        PlotterProfitabilityScope scope
) {
    public GetPlotterProfitabilityQuery(LocalDate fromDate, LocalDate toDate, String scope) {
        this(fromDate, toDate, PlotterProfitabilityScope.of(scope));
    }
}
