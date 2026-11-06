package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.CancelPlotterJobResult;
import com.magyen.platform.plotter.application.dto.EditPlotterJobCommand;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.usecase.CancelPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.CorrectPlotterJobDateUseCase;
import com.magyen.platform.plotter.application.usecase.EditPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.request.CorrectPlotterJobDateRequest;
import com.magyen.platform.plotter.presentation.plotterjob.request.EditPlotterJobRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.CancelPlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Correcciones de trabajos de Plotter. No crea cargos manuales ni pagos.
 */
@RestController
@RequestMapping("/api/v1/plotter/jobs")
public class PlotterJobMaintenanceController {

    private final EditPlotterJobUseCase editPlotterJobUseCase;
    private final CorrectPlotterJobDateUseCase correctPlotterJobDateUseCase;
    private final CancelPlotterJobUseCase cancelPlotterJobUseCase;
    private final GetPlotterJobUseCase getPlotterJobUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterJobMaintenanceController(
            EditPlotterJobUseCase editPlotterJobUseCase,
            CorrectPlotterJobDateUseCase correctPlotterJobDateUseCase,
            CancelPlotterJobUseCase cancelPlotterJobUseCase,
            GetPlotterJobUseCase getPlotterJobUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.editPlotterJobUseCase = editPlotterJobUseCase;
        this.correctPlotterJobDateUseCase = correctPlotterJobDateUseCase;
        this.cancelPlotterJobUseCase = cancelPlotterJobUseCase;
        this.getPlotterJobUseCase = getPlotterJobUseCase;
        this.plotterPresentationMapper = plotterPresentationMapper;
    }

    @PutMapping("/{plotterJobId}")
    public ResponseEntity<GetPlotterJobResponse> editPlotterJob(
            @PathVariable UUID plotterJobId,
            @RequestBody EditPlotterJobRequest request
    ) {
        editPlotterJobUseCase.execute(new EditPlotterJobCommand(
                plotterJobId,
                request.creationDate(),
                request.paperInventoryItemId(),
                request.printedMeters(),
                request.pricePerMeter(),
                request.commercialValue(),
                request.observations(),
                request.plotterMachineId()
        ));
        GetPlotterJobResult result = getPlotterJobUseCase.execute(
                plotterPresentationMapper.toQuery(plotterJobId)
        );
        return ResponseEntity.ok(plotterPresentationMapper.toGetResponse(result));
    }

    @PatchMapping("/{plotterJobId}/work-date")
    public ResponseEntity<GetPlotterJobResponse> correctWorkDate(
            @PathVariable UUID plotterJobId,
            @RequestBody CorrectPlotterJobDateRequest request
    ) {
        correctPlotterJobDateUseCase.execute(
                plotterPresentationMapper.toCorrectDateCommand(plotterJobId, request)
        );
        GetPlotterJobResult result = getPlotterJobUseCase.execute(
                plotterPresentationMapper.toQuery(plotterJobId)
        );
        return ResponseEntity.ok(plotterPresentationMapper.toGetResponse(result));
    }

    @PostMapping("/{plotterJobId}/cancellation")
    public ResponseEntity<CancelPlotterJobResponse> cancelPlotterJob(
            @PathVariable UUID plotterJobId
    ) {
        CancelPlotterJobResult result = cancelPlotterJobUseCase.execute(plotterJobId);
        return ResponseEntity.ok(new CancelPlotterJobResponse(
                result.plotterJobId(),
                result.deleted(),
                result.alreadyCancelled(),
                result.status() == null ? null : result.status().name()
        ));
    }
}
