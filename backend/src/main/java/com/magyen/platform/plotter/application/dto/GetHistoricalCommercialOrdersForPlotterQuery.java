package com.magyen.platform.plotter.application.dto;

/**
 * Mes de confirmación para buscar pedidos entregados o cerrados.
 */
public record GetHistoricalCommercialOrdersForPlotterQuery(
        int year,
        int month
) {
}
