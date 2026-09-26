package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.usecase.GetPlotterPendingBalancesUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterPendingBalancesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lectura all-time de saldos pendientes de trabajos externos de Plotter.
 * <p>
 * No crea cuentas por cobrar ni movimientos financieros.
 */
@RestController
@RequestMapping("/api/v1/plotter/pending-balances")
public class PlotterPendingBalanceController {

    private final GetPlotterPendingBalancesUseCase getPlotterPendingBalancesUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterPendingBalanceController(
            GetPlotterPendingBalancesUseCase getPlotterPendingBalancesUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.getPlotterPendingBalancesUseCase = getPlotterPendingBalancesUseCase;
        this.plotterPresentationMapper = plotterPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetPlotterPendingBalancesResponse> getPendingBalances() {
        return ResponseEntity.ok(plotterPresentationMapper.toPendingBalancesResponse(
                getPlotterPendingBalancesUseCase.execute()
        ));
    }
}
