package com.magyen.platform.commercial.presentation.quotation.controller;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemResult;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.GetQuotationCommand;
import com.magyen.platform.commercial.application.dto.GetQuotationResult;
import com.magyen.platform.commercial.application.dto.GetQuotationsQuery;
import com.magyen.platform.commercial.application.dto.GetQuotationsResult;
import com.magyen.platform.commercial.application.dto.CommercialDocumentPdfResult;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemResult;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemResult;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetQuotationsUseCase;
import com.magyen.platform.commercial.application.usecase.GenerateQuotationPdfUseCase;
import com.magyen.platform.commercial.application.usecase.RemoveQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateQuotationItemUseCase;
import com.magyen.platform.commercial.presentation.quotation.mapper.QuotationPresentationMapper;
import com.magyen.platform.commercial.presentation.quotation.request.AddQuotationItemRequest;
import com.magyen.platform.commercial.presentation.quotation.request.CreateQuotationRequest;
import com.magyen.platform.commercial.presentation.quotation.response.AddQuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ApproveQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.CreateQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.GetQuotationsResponse;
import com.magyen.platform.commercial.presentation.quotation.response.RemoveQuotationItemResponse;
import com.magyen.platform.commercial.presentation.quotation.response.UpdateQuotationItemResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    private final AddQuotationItemUseCase addQuotationItemUseCase;
    private final UpdateQuotationItemUseCase updateQuotationItemUseCase;
    private final RemoveQuotationItemUseCase removeQuotationItemUseCase;
    private final GetQuotationsUseCase getQuotationsUseCase;
    private final GetQuotationUseCase getQuotationUseCase;
    private final GenerateQuotationPdfUseCase generateQuotationPdfUseCase;
    private final QuotationPresentationMapper quotationPresentationMapper;

    public QuotationController(
            CreateQuotationUseCase createQuotationUseCase,
            ApproveQuotationUseCase approveQuotationUseCase,
            AddQuotationItemUseCase addQuotationItemUseCase,
            UpdateQuotationItemUseCase updateQuotationItemUseCase,
            RemoveQuotationItemUseCase removeQuotationItemUseCase,
            GetQuotationsUseCase getQuotationsUseCase,
            GetQuotationUseCase getQuotationUseCase,
            GenerateQuotationPdfUseCase generateQuotationPdfUseCase,
            QuotationPresentationMapper quotationPresentationMapper
    ) {
        this.createQuotationUseCase = createQuotationUseCase;
        this.approveQuotationUseCase = approveQuotationUseCase;
        this.addQuotationItemUseCase = addQuotationItemUseCase;
        this.updateQuotationItemUseCase = updateQuotationItemUseCase;
        this.removeQuotationItemUseCase = removeQuotationItemUseCase;
        this.getQuotationsUseCase = getQuotationsUseCase;
        this.getQuotationUseCase = getQuotationUseCase;
        this.generateQuotationPdfUseCase = generateQuotationPdfUseCase;
        this.quotationPresentationMapper = quotationPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetQuotationsResponse> getQuotations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetQuotationsResult result = getQuotationsUseCase.execute(new GetQuotationsQuery(fromDate, toDate));
        GetQuotationsResponse response = quotationPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{quotationId}")
    public ResponseEntity<GetQuotationResponse> getQuotation(
            @PathVariable UUID quotationId
    ) {
        GetQuotationCommand command = quotationPresentationMapper.toGetQuotationCommand(quotationId);
        GetQuotationResult result = getQuotationUseCase.execute(command);
        GetQuotationResponse response = quotationPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{quotationId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getQuotationPdf(@PathVariable UUID quotationId) {
        GetQuotationCommand command = quotationPresentationMapper.toGetQuotationCommand(quotationId);
        CommercialDocumentPdfResult result = generateQuotationPdfUseCase.execute(command);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(result.filename()))
                .body(result.content());
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

    @PostMapping("/{quotationId}/items")
    public ResponseEntity<AddQuotationItemResponse> addQuotationItem(
            @PathVariable UUID quotationId,
            @RequestBody AddQuotationItemRequest request
    ) {
        AddQuotationItemCommand command = quotationPresentationMapper.toAddItemCommand(quotationId, request);
        AddQuotationItemResult result = addQuotationItemUseCase.execute(command);
        AddQuotationItemResponse response = quotationPresentationMapper.toAddItemResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{quotationId}/items/{itemId}")
    public ResponseEntity<UpdateQuotationItemResponse> updateQuotationItem(
            @PathVariable UUID quotationId,
            @PathVariable UUID itemId,
            @RequestBody AddQuotationItemRequest request
    ) {
        UpdateQuotationItemCommand command = quotationPresentationMapper.toUpdateItemCommand(
                quotationId,
                itemId,
                request
        );
        UpdateQuotationItemResult result = updateQuotationItemUseCase.execute(command);
        UpdateQuotationItemResponse response = quotationPresentationMapper.toUpdateItemResponse(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{quotationId}/items/{itemId}")
    public ResponseEntity<RemoveQuotationItemResponse> removeQuotationItem(
            @PathVariable UUID quotationId,
            @PathVariable UUID itemId
    ) {
        RemoveQuotationItemCommand command = quotationPresentationMapper.toRemoveItemCommand(quotationId, itemId);
        RemoveQuotationItemResult result = removeQuotationItemUseCase.execute(command);
        RemoveQuotationItemResponse response = quotationPresentationMapper.toRemoveItemResponse(result);

        return ResponseEntity.ok(response);
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

    private static String contentDisposition(String filename) {
        return "attachment; filename=\"" + filename + "\"";
    }
}
