package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobsUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.request.CreatePlotterJobRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.CreatePlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de trabajos de plotter.
 */
@RestController
@RequestMapping("/api/v1/plotter/jobs")
public class PlotterJobController {

    private final CreatePlotterJobUseCase createPlotterJobUseCase;
    private final GetPlotterJobsUseCase getPlotterJobsUseCase;
    private final GetPlotterJobUseCase getPlotterJobUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterJobController(
            CreatePlotterJobUseCase createPlotterJobUseCase,
            GetPlotterJobsUseCase getPlotterJobsUseCase,
            GetPlotterJobUseCase getPlotterJobUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.createPlotterJobUseCase = createPlotterJobUseCase;
        this.getPlotterJobsUseCase = getPlotterJobsUseCase;
        this.getPlotterJobUseCase = getPlotterJobUseCase;
        this.plotterPresentationMapper = plotterPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<CreatePlotterJobResponse> createPlotterJob(
            @RequestBody CreatePlotterJobRequest request
    ) {
        CreatePlotterJobResult result = createPlotterJobUseCase.execute(
                plotterPresentationMapper.toCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plotterPresentationMapper.toCreateResponse(result));
    }

    @GetMapping
    public ResponseEntity<GetPlotterJobsResponse> getPlotterJobs() {
        GetPlotterJobsResult result = getPlotterJobsUseCase.execute();
        return ResponseEntity.ok(plotterPresentationMapper.toGetJobsResponse(result));
    }

    @GetMapping("/{plotterJobId}")
    public ResponseEntity<GetPlotterJobResponse> getPlotterJob(
            @PathVariable UUID plotterJobId
    ) {
        GetPlotterJobResult result = getPlotterJobUseCase.execute(
                plotterPresentationMapper.toQuery(plotterJobId)
        );
        return ResponseEntity.ok(plotterPresentationMapper.toGetResponse(result));
    }
}
