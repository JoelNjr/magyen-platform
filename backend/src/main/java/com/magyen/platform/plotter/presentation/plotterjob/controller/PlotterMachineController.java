package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.GetPlotterMachineOperationsQuery;
import com.magyen.platform.plotter.application.dto.PlotterMachineOperationResult;
import com.magyen.platform.plotter.application.dto.PlotterMachineResult;
import com.magyen.platform.plotter.application.usecase.ChangePlotterMachineActiveUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterMachineOperationsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterMachinesUseCase;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.plotter.presentation.plotterjob.request.ChangePlotterMachineActiveRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterMachineOperationsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterMachinesResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.PlotterMachineOperationResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.PlotterMachineResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Máquinas de Plotter y totales operativos. No crea movimientos financieros.
 */
@RestController
@RequestMapping("/api/v1/plotter")
public class PlotterMachineController {

    static final String UNIDENTIFIED_MACHINE_LABEL = "Sin identificar";

    private final GetPlotterMachinesUseCase getPlotterMachinesUseCase;
    private final ChangePlotterMachineActiveUseCase changePlotterMachineActiveUseCase;
    private final GetPlotterMachineOperationsUseCase getPlotterMachineOperationsUseCase;

    public PlotterMachineController(
            GetPlotterMachinesUseCase getPlotterMachinesUseCase,
            ChangePlotterMachineActiveUseCase changePlotterMachineActiveUseCase,
            GetPlotterMachineOperationsUseCase getPlotterMachineOperationsUseCase
    ) {
        this.getPlotterMachinesUseCase = getPlotterMachinesUseCase;
        this.changePlotterMachineActiveUseCase = changePlotterMachineActiveUseCase;
        this.getPlotterMachineOperationsUseCase = getPlotterMachineOperationsUseCase;
    }

    @GetMapping("/machines")
    public ResponseEntity<GetPlotterMachinesResponse> getMachines() {
        return ResponseEntity.ok(new GetPlotterMachinesResponse(
                getPlotterMachinesUseCase.execute().stream()
                        .map(PlotterMachineController::toResponse)
                        .toList()
        ));
    }

    @PatchMapping("/machines/{plotterMachineId}/active")
    public ResponseEntity<PlotterMachineResponse> changeActive(
            @PathVariable UUID plotterMachineId,
            @RequestBody ChangePlotterMachineActiveRequest request
    ) {
        if (request == null || request.active() == null) {
            throw new PlotterDomainException("Active flag must not be null");
        }
        return ResponseEntity.ok(toResponse(
                changePlotterMachineActiveUseCase.execute(plotterMachineId, request.active())
        ));
    }

    @GetMapping("/machine-operations")
    public ResponseEntity<GetPlotterMachineOperationsResponse> getOperations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID machineId,
            @RequestParam(required = false, defaultValue = "false") boolean unassignedMachine
    ) {
        var result = getPlotterMachineOperationsUseCase.execute(
                new GetPlotterMachineOperationsQuery(fromDate, toDate, machineId, unassignedMachine)
        );
        return ResponseEntity.ok(new GetPlotterMachineOperationsResponse(
                result.fromDate(),
                result.toDate(),
                result.machines().stream().map(PlotterMachineController::toOperation).toList()
        ));
    }

    private static PlotterMachineResponse toResponse(PlotterMachineResult result) {
        return new PlotterMachineResponse(
                result.plotterMachineId(),
                result.name(),
                result.active(),
                result.defaultForRegistration()
        );
    }

    private static PlotterMachineOperationResponse toOperation(PlotterMachineOperationResult result) {
        return new PlotterMachineOperationResponse(
                result.plotterMachineId(),
                result.identified() ? result.machineName() : UNIDENTIFIED_MACHINE_LABEL,
                result.identified(),
                result.jobCount(),
                result.printedMeters(),
                result.wasteMeters(),
                result.externalRevenue()
        );
    }
}
