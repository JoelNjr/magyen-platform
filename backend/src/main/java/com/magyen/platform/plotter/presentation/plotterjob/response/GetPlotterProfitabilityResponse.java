package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen analítico HTTP de Plotter.
 */
public record GetPlotterProfitabilityResponse(
        LocalDate fromDate,
        LocalDate toDate,
        String scope,
        int jobCount,
        int externalJobCount,
        int internalJobCount,
        BigDecimal totalPaperPrintedMeters,
        BigDecimal externalRevenue,
        BigDecimal externalPaperCost,
        BigDecimal internalPaperCost,
        BigDecimal totalPaperCost,
        BigDecimal internalPaperPrintedMeters,
        int unvaluedPaperJobCount,
        boolean paperCostComplete,
        boolean inkCostRecorded,
        BigDecimal inkCost,
        BigDecimal analyticalPlotterResult,
        List<PlotterInternalOrderCostItemResponse> internalOrders
) {
}
