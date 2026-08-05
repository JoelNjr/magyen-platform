package com.magyen.platform.commercial.presentation.quotation.controller;

import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.presentation.quotation.mapper.QuotationPresentationMapper;
import com.magyen.platform.commercial.presentation.quotation.request.CreateQuotationRequest;
import com.magyen.platform.commercial.presentation.quotation.response.ApproveQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.CreateQuotationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de cotizaciones.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final CreateQuotationUseCase createQuotationUseCase;
    private final ApproveQuotationUseCase approveQuotationUseCase;
    private final QuotationPresentationMapper quotationPresentationMapper;

    public QuotationController(
            CreateQuotationUseCase createQuotationUseCase,
            ApproveQuotationUseCase approveQuotationUseCase,
            QuotationPresentationMapper quotationPresentationMapper
    ) {
        this.createQuotationUseCase = createQuotationUseCase;
        this.approveQuotationUseCase = approveQuotationUseCase;
        this.quotationPresentationMapper = quotationPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<CreateQuotationResponse> createQuotation(
            @RequestBody CreateQuotationRequest request
    ) {
        CreateQuotationCommand command = quotationPresentationMapper.toCommand(request);
        CreateQuotationResult result = createQuotationUseCase.execute(command);
        CreateQuotationResponse response = quotationPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{quotationId}/approve")
    public ResponseEntity<ApproveQuotationResponse> approveQuotation(
            @PathVariable UUID quotationId
    ) {
        ApproveQuotationCommand command = quotationPresentationMapper.toApproveCommand(quotationId);
        ApproveQuotationResult result = approveQuotationUseCase.execute(command);
        ApproveQuotationResponse response = quotationPresentationMapper.toApproveResponse(result);

        return ResponseEntity.ok(response);
    }
}
