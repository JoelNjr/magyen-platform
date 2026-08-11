package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.domain.PlotterJob;

import java.math.BigDecimal;

/**
 * Mapeo compartido de {@link PlotterJob} a resultados de aplicación.
 */
final class PlotterJobReadMapper {

    private PlotterJobReadMapper() {
    }

    static CreatePlotterJobResult toCreateResult(PlotterJob plotterJob) {
        return new CreatePlotterJobResult(
                plotterJob.getId(),
                plotterJob.getCustomerId(),
                plotterJob.getCreationDate(),
                plotterJob.getPaperInventoryItemId(),
                plotterJob.getPrintedMeters(),
                plotterJob.getPricePerMeter(),
                plotterJob.getTotalAmount(),
                plotterJob.getStatus(),
                plotterJob.getObservations()
        );
    }

    static GetPlotterJobResult toGetResult(
            PlotterJob plotterJob,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {
        return new GetPlotterJobResult(
                plotterJob.getId(),
                plotterJob.getCustomerId(),
                plotterJob.getCreationDate(),
                plotterJob.getPaperInventoryItemId(),
                plotterJob.getPrintedMeters(),
                plotterJob.getPricePerMeter(),
                plotterJob.getTotalAmount(),
                paidAmount,
                outstandingAmount,
                plotterJob.getStatus(),
                plotterJob.getObservations()
        );
    }
}
