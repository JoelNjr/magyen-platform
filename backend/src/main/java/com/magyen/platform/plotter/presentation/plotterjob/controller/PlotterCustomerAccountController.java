package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.GetPlotterCustomerAccountResult;
import com.magyen.platform.plotter.application.dto.GetPlotterCustomerAccountsResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterCustomerPaymentResult;
import com.magyen.platform.plotter.application.usecase.GetPlotterCustomerAccountUseCase;
import com.magyen.platform.plotter.application.usecase.RegisterPlotterCustomerPaymentUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.request.RegisterPlotterCustomerPaymentRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterCustomerAccountResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterCustomerAccountsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.RegisterPlotterCustomerPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Cuenta corriente de clientes externos de Plotter.
 * <p>
 * Los débitos nacen de los trabajos. Este recurso solo registra pagos recibidos.
 * No existe un endpoint de cargos manuales.
 */
@RestController
@RequestMapping("/api/v1/plotter/customer-accounts")
public class PlotterCustomerAccountController {

    private final GetPlotterCustomerAccountUseCase getPlotterCustomerAccountUseCase;
    private final RegisterPlotterCustomerPaymentUseCase registerPlotterCustomerPaymentUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterCustomerAccountController(
            GetPlotterCustomerAccountUseCase getPlotterCustomerAccountUseCase,
            RegisterPlotterCustomerPaymentUseCase registerPlotterCustomerPaymentUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.getPlotterCustomerAccountUseCase = getPlotterCustomerAccountUseCase;
        this.registerPlotterCustomerPaymentUseCase = registerPlotterCustomerPaymentUseCase;
        this.plotterPresentationMapper = plotterPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetPlotterCustomerAccountsResponse> list(
            @RequestParam(required = false) UUID customerId
    ) {
        GetPlotterCustomerAccountsResult result = getPlotterCustomerAccountUseCase.list(customerId);
        return ResponseEntity.ok(plotterPresentationMapper.toCustomerAccountsResponse(result));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<GetPlotterCustomerAccountResponse> detail(
            @PathVariable UUID customerId
    ) {
        GetPlotterCustomerAccountResult result = getPlotterCustomerAccountUseCase.detail(customerId);
        return ResponseEntity.ok(plotterPresentationMapper.toCustomerAccountResponse(result));
    }

    @PostMapping("/{customerId}/payments")
    public ResponseEntity<RegisterPlotterCustomerPaymentResponse> registerPayment(
            @PathVariable UUID customerId,
            @RequestBody RegisterPlotterCustomerPaymentRequest request
    ) {
        RegisterPlotterCustomerPaymentResult result = registerPlotterCustomerPaymentUseCase.execute(
                plotterPresentationMapper.toCustomerPaymentCommand(customerId, request)
        );
        HttpStatus status = result.alreadyRegistered() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .body(plotterPresentationMapper.toCustomerPaymentResponse(result));
    }
}
