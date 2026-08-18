package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityResult;
import com.magyen.platform.plotter.application.usecase.GetPlotterProfitabilityUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterProfitabilityResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Lectura analítica de Plotter. No modifica inventario ni Finance.
 */
@RestController
@RequestMapping("/api/v1/plotter/profitability")
public class PlotterProfitabilityController {

    private final GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterProfitabilityController(
            GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.getPlotterProfitabilityUseCase = getPlotterProfitabilityUseCase;
        this.plotterPresentationMapper = plotterPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetPlotterProfitabilityResponse> getProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String scope
    ) {
        GetPlotterProfitabilityQuery query = new GetPlotterProfitabilityQuery(fromDate, toDate, scope);
        GetPlotterProfitabilityResult result = getPlotterProfitabilityUseCase.execute(query);
        return ResponseEntity.ok(plotterPresentationMapper.toProfitabilityResponse(result));
    }
}
