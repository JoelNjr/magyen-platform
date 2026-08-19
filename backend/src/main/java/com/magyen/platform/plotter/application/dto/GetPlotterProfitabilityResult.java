package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen analítico de Plotter. No crea transacciones Finance.
 * <p>
 * {@code inkCostRecorded} es false en V1: Plotter no registra consumo de tinta.
 * {@code totalPaperCost} es la suma de adquisiciones de papel en el período, no de OUTs.
 * {@code analyticalPlotterResult} es ingreso combinado menos adquisiciones de papel.
 */
public record GetPlotterProfitabilityResult(
        LocalDate fromDate,
        LocalDate toDate,
        PlotterProfitabilityScope scope,
        int jobCount,
        int externalJobCount,
        int internalJobCount,
        BigDecimal totalPaperPrintedMeters,
        BigDecimal externalRevenue,
        BigDecimal internalRevenue,
        BigDecimal combinedRevenue,
        BigDecimal externalPaperCost,
        BigDecimal internalPaperCost,
        BigDecimal totalPaperCost,
        BigDecimal internalPaperPrintedMeters,
        int unvaluedPaperJobCount,
        boolean paperCostComplete,
        boolean inkCostRecorded,
        BigDecimal inkCost,
        BigDecimal analyticalPlotterResult,
        List<PlotterInternalOrderCostItem> internalOrders
) {
}
