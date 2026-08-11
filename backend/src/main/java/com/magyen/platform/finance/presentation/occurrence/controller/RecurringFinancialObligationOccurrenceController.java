package com.magyen.platform.finance.presentation.occurrence.controller;

import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesCommand;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.usecase.CancelRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.GenerateRecurringFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetOverdueFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetPendingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetUpcomingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.PayRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.presentation.occurrence.mapper.RecurringFinancialObligationOccurrencePresentationMapper;
import com.magyen.platform.finance.presentation.occurrence.request.CreateRecurringFinancialObligationOccurrenceRequest;
import com.magyen.platform.finance.presentation.occurrence.request.GenerateRecurringFinancialObligationOccurrencesRequest;
import com.magyen.platform.finance.presentation.occurrence.request.PayRecurringFinancialObligationOccurrenceRequest;
import com.magyen.platform.finance.presentation.occurrence.response.CancelRecurringFinancialObligationOccurrenceResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GenerateRecurringFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetOverdueFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetPendingFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetRecurringFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.GetUpcomingFinancialObligationOccurrencesResponse;
import com.magyen.platform.finance.presentation.occurrence.response.PayRecurringFinancialObligationOccurrenceResponse;
import com.magyen.platform.finance.presentation.occurrence.response.RecurringFinancialObligationOccurrenceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de ocurrencias de obligaciones financieras recurrentes.
 * <p>
 * Crear o generar una ocurrencia no crea movimientos del ledger.
 * Solo el pago explícito genera un {@code FinancialTransaction}.
 */
@RestController
@RequestMapping("/api/v1/finance/obligation-occurrences")
public class RecurringFinancialObligationOccurrenceController {

    private final CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;
    private final GenerateRecurringFinancialObligationOccurrencesUseCase generateOccurrencesUseCase;
    private final GetRecurringFinancialObligationOccurrenceUseCase getOccurrenceUseCase;
    private final GetRecurringFinancialObligationOccurrencesUseCase getOccurrencesUseCase;
    private final GetPendingFinancialObligationOccurrencesUseCase getPendingOccurrencesUseCase;
    private final GetOverdueFinancialObligationOccurrencesUseCase getOverdueOccurrencesUseCase;
    private final GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingOccurrencesUseCase;
    private final PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase;
    private final CancelRecurringFinancialObligationOccurrenceUseCase cancelOccurrenceUseCase;
    private final RecurringFinancialObligationOccurrencePresentationMapper presentationMapper;

    public RecurringFinancialObligationOccurrenceController(
            CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase,
            GenerateRecurringFinancialObligationOccurrencesUseCase generateOccurrencesUseCase,
            GetRecurringFinancialObligationOccurrenceUseCase getOccurrenceUseCase,
            GetRecurringFinancialObligationOccurrencesUseCase getOccurrencesUseCase,
            GetPendingFinancialObligationOccurrencesUseCase getPendingOccurrencesUseCase,
            GetOverdueFinancialObligationOccurrencesUseCase getOverdueOccurrencesUseCase,
            GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingOccurrencesUseCase,
            PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase,
            CancelRecurringFinancialObligationOccurrenceUseCase cancelOccurrenceUseCase,
            RecurringFinancialObligationOccurrencePresentationMapper presentationMapper
    ) {
        this.createOccurrenceUseCase = createOccurrenceUseCase;
        this.generateOccurrencesUseCase = generateOccurrencesUseCase;
        this.getOccurrenceUseCase = getOccurrenceUseCase;
        this.getOccurrencesUseCase = getOccurrencesUseCase;
        this.getPendingOccurrencesUseCase = getPendingOccurrencesUseCase;
        this.getOverdueOccurrencesUseCase = getOverdueOccurrencesUseCase;
        this.getUpcomingOccurrencesUseCase = getUpcomingOccurrencesUseCase;
        this.payOccurrenceUseCase = payOccurrenceUseCase;
        this.cancelOccurrenceUseCase = cancelOccurrenceUseCase;
        this.presentationMapper = presentationMapper;
    }

    @PostMapping
    public ResponseEntity<RecurringFinancialObligationOccurrenceResponse> createOccurrence(
            @RequestBody CreateRecurringFinancialObligationOccurrenceRequest request
    ) {
        CreateRecurringFinancialObligationOccurrenceCommand command = presentationMapper.toCommand(request);
        CreateRecurringFinancialObligationOccurrenceResult result = createOccurrenceUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(presentationMapper.toResponse(result));
    }

    /**
     * Generación controlada e idempotente. No crea movimientos del ledger.
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateRecurringFinancialObligationOccurrencesResponse> generateOccurrences(
            @RequestBody GenerateRecurringFinancialObligationOccurrencesRequest request
    ) {
        GenerateRecurringFinancialObligationOccurrencesCommand command =
                presentationMapper.toGenerateCommand(request);
        GenerateRecurringFinancialObligationOccurrencesResult result =
                generateOccurrencesUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping("/pending")
    public ResponseEntity<GetPendingFinancialObligationOccurrencesResponse> getPendingOccurrences() {
        GetPendingFinancialObligationOccurrencesQuery query = presentationMapper.toPendingQuery();
        GetPendingFinancialObligationOccurrencesResult result = getPendingOccurrencesUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping("/overdue")
    public ResponseEntity<GetOverdueFinancialObligationOccurrencesResponse> getOverdueOccurrences() {
        GetOverdueFinancialObligationOccurrencesQuery query = presentationMapper.toOverdueQuery();
        GetOverdueFinancialObligationOccurrencesResult result = getOverdueOccurrencesUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<GetUpcomingFinancialObligationOccurrencesResponse> getUpcomingOccurrences(
            @RequestParam(required = false) Integer daysAhead
    ) {
        GetUpcomingFinancialObligationOccurrencesQuery query = presentationMapper.toUpcomingQuery(daysAhead);
        GetUpcomingFinancialObligationOccurrencesResult result = getUpcomingOccurrencesUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<GetRecurringFinancialObligationOccurrencesResponse> getOccurrences(
            @RequestParam(required = false) String status
    ) {
        GetRecurringFinancialObligationOccurrencesQuery query = presentationMapper.toListQuery(status);
        GetRecurringFinancialObligationOccurrencesResult result = getOccurrencesUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @GetMapping("/{occurrenceId}")
    public ResponseEntity<RecurringFinancialObligationOccurrenceResponse> getOccurrence(
            @PathVariable UUID occurrenceId
    ) {
        GetRecurringFinancialObligationOccurrenceQuery query = presentationMapper.toGetQuery(occurrenceId);
        GetRecurringFinancialObligationOccurrenceResult result = getOccurrenceUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @PatchMapping("/{occurrenceId}/pay")
    public ResponseEntity<PayRecurringFinancialObligationOccurrenceResponse> payOccurrence(
            @PathVariable UUID occurrenceId,
            @RequestBody(required = false) PayRecurringFinancialObligationOccurrenceRequest request
    ) {
        PayRecurringFinancialObligationOccurrenceCommand command =
                presentationMapper.toPayCommand(occurrenceId, request);
        PayRecurringFinancialObligationOccurrenceResult result = payOccurrenceUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }

    @PatchMapping("/{occurrenceId}/cancel")
    public ResponseEntity<CancelRecurringFinancialObligationOccurrenceResponse> cancelOccurrence(
            @PathVariable UUID occurrenceId
    ) {
        CancelRecurringFinancialObligationOccurrenceCommand command =
                presentationMapper.toCancelCommand(occurrenceId);
        CancelRecurringFinancialObligationOccurrenceResult result = cancelOccurrenceUseCase.execute(command);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }
}
