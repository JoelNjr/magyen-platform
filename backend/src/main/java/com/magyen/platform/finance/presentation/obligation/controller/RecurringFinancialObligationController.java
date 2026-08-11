package com.magyen.platform.finance.presentation.obligation.controller;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsResult;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.DeactivateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.GetRecurringFinancialObligationsUseCase;
import com.magyen.platform.finance.application.usecase.UpdateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.presentation.obligation.mapper.RecurringFinancialObligationPresentationMapper;
import com.magyen.platform.finance.presentation.obligation.request.CreateRecurringFinancialObligationRequest;
import com.magyen.platform.finance.presentation.obligation.request.UpdateRecurringFinancialObligationRequest;
import com.magyen.platform.finance.presentation.obligation.response.DeactivateRecurringFinancialObligationResponse;
import com.magyen.platform.finance.presentation.obligation.response.GetRecurringFinancialObligationsResponse;
import com.magyen.platform.finance.presentation.obligation.response.RecurringFinancialObligationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de obligaciones financieras recurrentes.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 * Crear una obligación no crea movimientos del ledger.
 */
@RestController
@RequestMapping("/api/v1/finance/obligations")
public class RecurringFinancialObligationController {

    private final CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase;
    private final GetRecurringFinancialObligationUseCase getRecurringFinancialObligationUseCase;
    private final GetRecurringFinancialObligationsUseCase getRecurringFinancialObligationsUseCase;
    private final UpdateRecurringFinancialObligationUseCase updateRecurringFinancialObligationUseCase;
    private final DeactivateRecurringFinancialObligationUseCase deactivateRecurringFinancialObligationUseCase;
    private final RecurringFinancialObligationPresentationMapper recurringFinancialObligationPresentationMapper;

    public RecurringFinancialObligationController(
            CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase,
            GetRecurringFinancialObligationUseCase getRecurringFinancialObligationUseCase,
            GetRecurringFinancialObligationsUseCase getRecurringFinancialObligationsUseCase,
            UpdateRecurringFinancialObligationUseCase updateRecurringFinancialObligationUseCase,
            DeactivateRecurringFinancialObligationUseCase deactivateRecurringFinancialObligationUseCase,
            RecurringFinancialObligationPresentationMapper recurringFinancialObligationPresentationMapper
    ) {
        this.createRecurringFinancialObligationUseCase = createRecurringFinancialObligationUseCase;
        this.getRecurringFinancialObligationUseCase = getRecurringFinancialObligationUseCase;
        this.getRecurringFinancialObligationsUseCase = getRecurringFinancialObligationsUseCase;
        this.updateRecurringFinancialObligationUseCase = updateRecurringFinancialObligationUseCase;
        this.deactivateRecurringFinancialObligationUseCase = deactivateRecurringFinancialObligationUseCase;
        this.recurringFinancialObligationPresentationMapper = recurringFinancialObligationPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<RecurringFinancialObligationResponse> createObligation(
            @RequestBody CreateRecurringFinancialObligationRequest request
    ) {
        CreateRecurringFinancialObligationCommand command =
                recurringFinancialObligationPresentationMapper.toCommand(request);
        CreateRecurringFinancialObligationResult result =
                createRecurringFinancialObligationUseCase.execute(command);
        RecurringFinancialObligationResponse response =
                recurringFinancialObligationPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GetRecurringFinancialObligationsResponse> getObligations(
            @RequestParam(required = false) Boolean active
    ) {
        GetRecurringFinancialObligationsQuery query =
                recurringFinancialObligationPresentationMapper.toListQuery(active);
        GetRecurringFinancialObligationsResult result =
                getRecurringFinancialObligationsUseCase.execute(query);
        GetRecurringFinancialObligationsResponse response =
                recurringFinancialObligationPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{obligationId}")
    public ResponseEntity<RecurringFinancialObligationResponse> getObligation(
            @PathVariable UUID obligationId
    ) {
        GetRecurringFinancialObligationQuery query =
                recurringFinancialObligationPresentationMapper.toGetQuery(obligationId);
        GetRecurringFinancialObligationResult result =
                getRecurringFinancialObligationUseCase.execute(query);
        RecurringFinancialObligationResponse response =
                recurringFinancialObligationPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{obligationId}")
    public ResponseEntity<RecurringFinancialObligationResponse> updateObligation(
            @PathVariable UUID obligationId,
            @RequestBody UpdateRecurringFinancialObligationRequest request
    ) {
        UpdateRecurringFinancialObligationCommand command =
                recurringFinancialObligationPresentationMapper.toUpdateCommand(obligationId, request);
        UpdateRecurringFinancialObligationResult result =
                updateRecurringFinancialObligationUseCase.execute(command);
        RecurringFinancialObligationResponse response =
                recurringFinancialObligationPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{obligationId}/deactivate")
    public ResponseEntity<DeactivateRecurringFinancialObligationResponse> deactivateObligation(
            @PathVariable UUID obligationId
    ) {
        DeactivateRecurringFinancialObligationCommand command =
                recurringFinancialObligationPresentationMapper.toDeactivateCommand(obligationId);
        DeactivateRecurringFinancialObligationResult result =
                deactivateRecurringFinancialObligationUseCase.execute(command);
        DeactivateRecurringFinancialObligationResponse response =
                recurringFinancialObligationPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
