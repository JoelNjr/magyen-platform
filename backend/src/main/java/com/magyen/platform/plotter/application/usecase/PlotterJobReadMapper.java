package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.domain.PlotterJob;

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

    static GetPlotterJobResult toGetResult(PlotterJob plotterJob) {
        return new GetPlotterJobResult(
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
}
