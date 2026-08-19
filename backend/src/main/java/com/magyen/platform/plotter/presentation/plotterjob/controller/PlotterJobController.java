package com.magyen.platform.plotter.presentation.plotterjob.controller;

import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterJobsResult;
import com.magyen.platform.plotter.application.dto.GetPlotterPaymentsResult;
import com.magyen.platform.plotter.application.dto.RegisterPlotterPaymentResult;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterJobsUseCase;
import com.magyen.platform.plotter.application.usecase.GetPlotterPaymentsUseCase;
import com.magyen.platform.plotter.application.usecase.RegisterPlotterPaymentUseCase;
import com.magyen.platform.plotter.presentation.plotterjob.mapper.PlotterPresentationMapper;
import com.magyen.platform.plotter.presentation.plotterjob.request.CreatePlotterJobRequest;
import com.magyen.platform.plotter.presentation.plotterjob.request.RegisterPlotterPaymentRequest;
import com.magyen.platform.plotter.presentation.plotterjob.response.CreatePlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterJobsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.GetPlotterPaymentsResponse;
import com.magyen.platform.plotter.presentation.plotterjob.response.RegisterPlotterPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Expone la API REST de trabajos de plotter y sus pagos.
 * <p>
 * Crear un trabajo no crea ingresos del ledger. Solo el registro de pago genera INCOME.
 */
@RestController
@RequestMapping("/api/v1/plotter/jobs")
public class PlotterJobController {

    private final CreatePlotterJobUseCase createPlotterJobUseCase;
    private final GetPlotterJobsUseCase getPlotterJobsUseCase;
    private final GetPlotterJobUseCase getPlotterJobUseCase;
    private final RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase;
    private final GetPlotterPaymentsUseCase getPlotterPaymentsUseCase;
    private final PlotterPresentationMapper plotterPresentationMapper;

    public PlotterJobController(
            CreatePlotterJobUseCase createPlotterJobUseCase,
            GetPlotterJobsUseCase getPlotterJobsUseCase,
            GetPlotterJobUseCase getPlotterJobUseCase,
            RegisterPlotterPaymentUseCase registerPlotterPaymentUseCase,
            GetPlotterPaymentsUseCase getPlotterPaymentsUseCase,
            PlotterPresentationMapper plotterPresentationMapper
    ) {
        this.createPlotterJobUseCase = createPlotterJobUseCase;
        this.getPlotterJobsUseCase = getPlotterJobsUseCase;
        this.getPlotterJobUseCase = getPlotterJobUseCase;
        this.registerPlotterPaymentUseCase = registerPlotterPaymentUseCase;
        this.getPlotterPaymentsUseCase = getPlotterPaymentsUseCase;
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
    public ResponseEntity<GetPlotterJobsResponse> getPlotterJobs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetPlotterJobsResult result = getPlotterJobsUseCase.execute(new GetPlotterJobsQuery(fromDate, toDate));
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

    @PostMapping("/{plotterJobId}/payments")
    public ResponseEntity<RegisterPlotterPaymentResponse> registerPayment(
            @PathVariable UUID plotterJobId,
            @RequestBody RegisterPlotterPaymentRequest request
    ) {
        RegisterPlotterPaymentResult result = registerPlotterPaymentUseCase.execute(
                plotterPresentationMapper.toRegisterPaymentCommand(plotterJobId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plotterPresentationMapper.toRegisterPaymentResponse(result));
    }

    @GetMapping("/{plotterJobId}/payments")
    public ResponseEntity<GetPlotterPaymentsResponse> getPayments(
            @PathVariable UUID plotterJobId
    ) {
        GetPlotterPaymentsResult result = getPlotterPaymentsUseCase.execute(
                plotterPresentationMapper.toPaymentsQuery(plotterJobId)
        );
        return ResponseEntity.ok(plotterPresentationMapper.toPaymentsResponse(result));
    }
}
