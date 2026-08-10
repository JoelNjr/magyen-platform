package com.magyen.platform.plotter.presentation.plotterjob.mapper;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.presentation.plotterjob.request.CreatePlotterJobRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.CreatePlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobsResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 */
public class PlotterPresentationMapper {

    public CreatePlotterJobCommand toCommand(CreatePlotterJobRequest request) {
        Objects.requireNonNull(request, "CreatePlotterJobRequest must not be null");

        return new CreatePlotterJobCommand(
                request.customerId(),
                request.paperInventoryItemId(),
                request.printedMeters(),
                request.pricePerMeter(),
                request.observations()
        );
    }

    public CreatePlotterJobResponse toCreateResponse(CreatePlotterJobResult result) {
        Objects.requireNonNull(result, "CreatePlotterJobResult must not be null");

        return new CreatePlotterJobResponse(
                result.plotterJobId(),
                result.customerId(),
                result.creationDate(),
                result.paperInventoryItemId(),
                result.printedMeters(),
                result.pricePerMeter(),
                result.totalAmount(),
                result.status().name(),
                result.observations()
        );
    }

    public GetPlotterJobQuery toQuery(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return new GetPlotterJobQuery(plotterJobId);
    }

    public GetPlotterJobResponse toGetResponse(GetPlotterJobResult result) {
        Objects.requireNonNull(result, "GetPlotterJobResult must not be null");

        return new GetPlotterJobResponse(
                result.plotterJobId(),
                result.customerId(),
                result.creationDate(),
                result.paperInventoryItemId(),
                result.printedMeters(),
                result.pricePerMeter(),
                result.totalAmount(),
                result.status().name(),
                result.observations()
        );
    }

    public GetPlotterJobsResponse toGetJobsResponse(GetPlotterJobsResult result) {
        Objects.requireNonNull(result, "GetPlotterJobsResult must not be null");

        return new GetPlotterJobsResponse(
                result.jobs().stream()
                        .map(this::toGetResponse)
                        .toList()
        );
    }
}
