package com.magyen.platform.production.presentation.productionorder.controller;

import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.AddProductionOperationResult;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorCommand;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorResult;
import com.magyen.platform.production.application.dto.CompleteProductionOperationCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOperationResult;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderResult;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.GetProductionLaborWorkQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.GetProductionOrdersQuery;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.GetProductionReferenceImageResult;
import com.magyen.platform.production.application.dto.ProductionDocumentPdfResult;
import com.magyen.platform.production.application.dto.RemoveProductionReferenceImageCommand;
import com.magyen.platform.production.application.dto.RemoveProductionReferenceImageResult;
import com.magyen.platform.production.application.dto.ReplaceProductionReferenceImageCommand;
import com.magyen.platform.production.application.dto.ReplaceProductionReferenceImageResult;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.PayProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderResult;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderResult;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CancelProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.GetProductionLaborWorksUseCase;
import com.magyen.platform.production.application.usecase.GetProductionMaterialConsumptionsUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.application.usecase.GenerateProductionOrderPdfUseCase;
import com.magyen.platform.production.application.usecase.GetProductionReferenceImageUseCase;
import com.magyen.platform.production.application.usecase.RemoveProductionReferenceImageUseCase;
import com.magyen.platform.production.application.usecase.ReplaceProductionReferenceImageUseCase;
import com.magyen.platform.production.application.usecase.PayProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import com.magyen.platform.production.presentation.productionorder.request.AddProductionOperationRequest;
import com.magyen.platform.production.presentation.productionorder.request.AssignProductionOperationOperatorRequest;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.PayProductionLaborWorkRequest;
import com.magyen.platform.production.presentation.productionorder.request.CompleteProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.PlanProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.StartProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.RegisterProductionLaborWorkRequest;
import com.magyen.platform.production.presentation.productionorder.request.RegisterProductionMaterialConsumptionRequest;
import com.magyen.platform.production.presentation.productionorder.response.AddProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.AssignProductionOperationOperatorResponse;
import com.magyen.platform.production.presentation.productionorder.response.CancelProductionLaborWorkResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionLaborWorkResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionLaborWorksResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionMaterialConsumptionsResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrdersResponse;
import com.magyen.platform.production.presentation.productionorder.response.PayProductionLaborWorkResponse;
import com.magyen.platform.production.presentation.productionorder.response.PlanProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.RemoveProductionReferenceImageResponse;
import com.magyen.platform.production.presentation.productionorder.response.ReplaceProductionReferenceImageResponse;
import com.magyen.platform.production.presentation.productionorder.response.RegisterProductionLaborWorkResponse;
import com.magyen.platform.production.presentation.productionorder.response.RegisterProductionMaterialConsumptionResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOrderResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Expone la API REST de órdenes de producción.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/production-orders")
public class ProductionOrderController {

    private final CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;
    private final GetProductionOrdersUseCase getProductionOrdersUseCase;
    private final GetProductionOrderUseCase getProductionOrderUseCase;
    private final GenerateProductionOrderPdfUseCase generateProductionOrderPdfUseCase;
    private final ReplaceProductionReferenceImageUseCase replaceProductionReferenceImageUseCase;
    private final RemoveProductionReferenceImageUseCase removeProductionReferenceImageUseCase;
    private final GetProductionReferenceImageUseCase getProductionReferenceImageUseCase;
    private final PlanProductionOrderUseCase planProductionOrderUseCase;
    private final StartProductionOrderUseCase startProductionOrderUseCase;
    private final CompleteProductionOrderUseCase completeProductionOrderUseCase;
    private final AddProductionOperationUseCase addProductionOperationUseCase;
    private final AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase;
    private final StartProductionOperationUseCase startProductionOperationUseCase;
    private final CompleteProductionOperationUseCase completeProductionOperationUseCase;
    private final RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;
    private final GetProductionMaterialConsumptionsUseCase getProductionMaterialConsumptionsUseCase;
    private final RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase;
    private final GetProductionLaborWorksUseCase getProductionLaborWorksUseCase;
    private final GetProductionLaborWorkUseCase getProductionLaborWorkUseCase;
    private final PayProductionLaborWorkUseCase payProductionLaborWorkUseCase;
    private final CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase;
    private final ProductionPresentationMapper productionPresentationMapper;

    public ProductionOrderController(
            CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase,
            GetProductionOrdersUseCase getProductionOrdersUseCase,
            GetProductionOrderUseCase getProductionOrderUseCase,
            GenerateProductionOrderPdfUseCase generateProductionOrderPdfUseCase,
            ReplaceProductionReferenceImageUseCase replaceProductionReferenceImageUseCase,
            RemoveProductionReferenceImageUseCase removeProductionReferenceImageUseCase,
            GetProductionReferenceImageUseCase getProductionReferenceImageUseCase,
            PlanProductionOrderUseCase planProductionOrderUseCase,
            StartProductionOrderUseCase startProductionOrderUseCase,
            CompleteProductionOrderUseCase completeProductionOrderUseCase,
            AddProductionOperationUseCase addProductionOperationUseCase,
            AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase,
            StartProductionOperationUseCase startProductionOperationUseCase,
            CompleteProductionOperationUseCase completeProductionOperationUseCase,
            RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase,
            GetProductionMaterialConsumptionsUseCase getProductionMaterialConsumptionsUseCase,
            RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase,
            GetProductionLaborWorksUseCase getProductionLaborWorksUseCase,
            GetProductionLaborWorkUseCase getProductionLaborWorkUseCase,
            PayProductionLaborWorkUseCase payProductionLaborWorkUseCase,
            CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase,
            ProductionPresentationMapper productionPresentationMapper
    ) {
        this.createProductionOrderFromOrderUseCase = createProductionOrderFromOrderUseCase;
        this.getProductionOrdersUseCase = getProductionOrdersUseCase;
        this.getProductionOrderUseCase = getProductionOrderUseCase;
        this.generateProductionOrderPdfUseCase = generateProductionOrderPdfUseCase;
        this.replaceProductionReferenceImageUseCase = replaceProductionReferenceImageUseCase;
        this.removeProductionReferenceImageUseCase = removeProductionReferenceImageUseCase;
        this.getProductionReferenceImageUseCase = getProductionReferenceImageUseCase;
        this.planProductionOrderUseCase = planProductionOrderUseCase;
        this.startProductionOrderUseCase = startProductionOrderUseCase;
        this.completeProductionOrderUseCase = completeProductionOrderUseCase;
        this.addProductionOperationUseCase = addProductionOperationUseCase;
        this.assignProductionOperationOperatorUseCase = assignProductionOperationOperatorUseCase;
        this.startProductionOperationUseCase = startProductionOperationUseCase;
        this.completeProductionOperationUseCase = completeProductionOperationUseCase;
        this.registerProductionMaterialConsumptionUseCase = registerProductionMaterialConsumptionUseCase;
        this.getProductionMaterialConsumptionsUseCase = getProductionMaterialConsumptionsUseCase;
        this.registerProductionLaborWorkUseCase = registerProductionLaborWorkUseCase;
        this.getProductionLaborWorksUseCase = getProductionLaborWorksUseCase;
        this.getProductionLaborWorkUseCase = getProductionLaborWorkUseCase;
        this.payProductionLaborWorkUseCase = payProductionLaborWorkUseCase;
        this.cancelProductionLaborWorkUseCase = cancelProductionLaborWorkUseCase;
        this.productionPresentationMapper = productionPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetProductionOrdersResponse> getProductionOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetProductionOrdersResult result = getProductionOrdersUseCase.execute(
                new GetProductionOrdersQuery(fromDate, toDate)
        );
        GetProductionOrdersResponse response = productionPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productionOrderId}")
    public ResponseEntity<GetProductionOrderResponse> getProductionOrder(
            @PathVariable UUID productionOrderId
    ) {
        GetProductionOrderCommand command = productionPresentationMapper.toGetProductionOrderCommand(productionOrderId);
        GetProductionOrderResult result = getProductionOrderUseCase.execute(command);
        GetProductionOrderResponse response = productionPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{productionOrderId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProductionOrderPdf(@PathVariable UUID productionOrderId) {
        GetProductionOrderCommand command = productionPresentationMapper.toGetProductionOrderCommand(productionOrderId);
        ProductionDocumentPdfResult result = generateProductionOrderPdfUseCase.execute(command);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(result.filename()))
                .body(result.content());
    }

    @GetMapping("/{productionOrderId}/reference-image")
    public ResponseEntity<byte[]> getProductionReferenceImage(@PathVariable UUID productionOrderId) {
        GetProductionOrderCommand command = productionPresentationMapper.toGetProductionOrderCommand(productionOrderId);
        GetProductionReferenceImageResult result = getProductionReferenceImageUseCase.execute(command);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(result.content());
    }

    @PutMapping(value = "/{productionOrderId}/reference-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReplaceProductionReferenceImageResponse> replaceProductionReferenceImage(
            @PathVariable UUID productionOrderId,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Reference image file is required");
        }
        ReplaceProductionReferenceImageCommand command = productionPresentationMapper.toReplaceReferenceImageCommand(
                productionOrderId,
                file.getOriginalFilename(),
                file.getContentType(),
                readFileBytes(file)
        );
        ReplaceProductionReferenceImageResult result = replaceProductionReferenceImageUseCase.execute(command);
        return ResponseEntity.ok(productionPresentationMapper.toReplaceReferenceImageResponse(result));
    }

    @DeleteMapping("/{productionOrderId}/reference-image")
    public ResponseEntity<RemoveProductionReferenceImageResponse> removeProductionReferenceImage(
            @PathVariable UUID productionOrderId
    ) {
        RemoveProductionReferenceImageResult result = removeProductionReferenceImageUseCase.execute(
                new RemoveProductionReferenceImageCommand(productionOrderId)
        );
        return ResponseEntity.ok(productionPresentationMapper.toRemoveReferenceImageResponse(result));
    }

    @PostMapping
    public ResponseEntity<CreateProductionOrderResponse> createProductionOrder(
            @RequestBody CreateProductionOrderRequest request
    ) {
        CreateProductionOrderCommand command = productionPresentationMapper.toCommand(request);
        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(command);
        CreateProductionOrderResponse response = productionPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productionOrderId}/plan")
    public ResponseEntity<PlanProductionOrderResponse> planProductionOrder(
            @PathVariable UUID productionOrderId,
            @RequestBody PlanProductionOrderRequest request
    ) {
        PlanProductionOrderCommand command = productionPresentationMapper.toPlanCommand(productionOrderId, request);
        PlanProductionOrderResult result = planProductionOrderUseCase.execute(command);
        PlanProductionOrderResponse response = productionPresentationMapper.toPlanResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/start")
    public ResponseEntity<StartProductionOrderResponse> startProductionOrder(
            @PathVariable UUID productionOrderId,
            @RequestBody(required = false) StartProductionOrderRequest request
    ) {
        StartProductionOrderCommand command = productionPresentationMapper.toStartOrderCommand(
                productionOrderId,
                request
        );
        StartProductionOrderResult result = startProductionOrderUseCase.execute(command);
        StartProductionOrderResponse response = productionPresentationMapper.toStartOrderResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/complete")
    public ResponseEntity<CompleteProductionOrderResponse> completeProductionOrder(
            @PathVariable UUID productionOrderId,
            @RequestBody(required = false) CompleteProductionOrderRequest request
    ) {
        CompleteProductionOrderCommand command = productionPresentationMapper.toCompleteOrderCommand(
                productionOrderId,
                request
        );
        CompleteProductionOrderResult result = completeProductionOrderUseCase.execute(command);
        CompleteProductionOrderResponse response = productionPresentationMapper.toCompleteOrderResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productionOrderId}/operations")
    public ResponseEntity<AddProductionOperationResponse> addProductionOperation(
            @PathVariable UUID productionOrderId,
            @RequestBody AddProductionOperationRequest request
    ) {
        AddProductionOperationCommand command = productionPresentationMapper.toAddOperationCommand(
                productionOrderId,
                request
        );
        AddProductionOperationResult result = addProductionOperationUseCase.execute(command);
        AddProductionOperationResponse response = productionPresentationMapper.toAddOperationResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productionOrderId}/operations/{operationId}/assign-operator")
    public ResponseEntity<AssignProductionOperationOperatorResponse> assignProductionOperationOperator(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID operationId,
            @RequestBody AssignProductionOperationOperatorRequest request
    ) {
        AssignProductionOperationOperatorCommand command = productionPresentationMapper.toAssignOperatorCommand(
                productionOrderId,
                operationId,
                request
        );
        AssignProductionOperationOperatorResult result = assignProductionOperationOperatorUseCase.execute(command);
        AssignProductionOperationOperatorResponse response =
                productionPresentationMapper.toAssignOperatorResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/operations/{operationId}/start")
    public ResponseEntity<StartProductionOperationResponse> startProductionOperation(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID operationId
    ) {
        StartProductionOperationCommand command = productionPresentationMapper.toStartOperationCommand(
                productionOrderId,
                operationId
        );
        StartProductionOperationResult result = startProductionOperationUseCase.execute(command);
        StartProductionOperationResponse response = productionPresentationMapper.toStartOperationResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/operations/{operationId}/complete")
    public ResponseEntity<CompleteProductionOperationResponse> completeProductionOperation(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID operationId
    ) {
        CompleteProductionOperationCommand command = productionPresentationMapper.toCompleteOperationCommand(
                productionOrderId,
                operationId
        );
        CompleteProductionOperationResult result = completeProductionOperationUseCase.execute(command);
        CompleteProductionOperationResponse response =
                productionPresentationMapper.toCompleteOperationResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productionOrderId}/material-consumptions")
    public ResponseEntity<RegisterProductionMaterialConsumptionResponse> registerMaterialConsumption(
            @PathVariable UUID productionOrderId,
            @RequestBody RegisterProductionMaterialConsumptionRequest request
    ) {
        RegisterProductionMaterialConsumptionCommand command =
                productionPresentationMapper.toRegisterMaterialConsumptionCommand(productionOrderId, request);
        RegisterProductionMaterialConsumptionResult result =
                registerProductionMaterialConsumptionUseCase.execute(command);
        RegisterProductionMaterialConsumptionResponse response =
                productionPresentationMapper.toRegisterMaterialConsumptionResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productionOrderId}/material-consumptions")
    public ResponseEntity<GetProductionMaterialConsumptionsResponse> getMaterialConsumptions(
            @PathVariable UUID productionOrderId
    ) {
        GetProductionMaterialConsumptionsQuery query =
                productionPresentationMapper.toMaterialConsumptionsQuery(productionOrderId);
        GetProductionMaterialConsumptionsResult result = getProductionMaterialConsumptionsUseCase.execute(query);
        GetProductionMaterialConsumptionsResponse response = productionPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productionOrderId}/labor")
    public ResponseEntity<RegisterProductionLaborWorkResponse> registerLaborWork(
            @PathVariable UUID productionOrderId,
            @RequestBody RegisterProductionLaborWorkRequest request
    ) {
        RegisterProductionLaborWorkCommand command =
                productionPresentationMapper.toRegisterLaborWorkCommand(productionOrderId, request);
        RegisterProductionLaborWorkResult result = registerProductionLaborWorkUseCase.execute(command);
        RegisterProductionLaborWorkResponse response =
                productionPresentationMapper.toRegisterLaborWorkResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productionOrderId}/labor")
    public ResponseEntity<GetProductionLaborWorksResponse> getLaborWorks(
            @PathVariable UUID productionOrderId
    ) {
        GetProductionLaborWorksQuery query = productionPresentationMapper.toLaborWorksQuery(productionOrderId);
        GetProductionLaborWorksResult result = getProductionLaborWorksUseCase.execute(query);
        GetProductionLaborWorksResponse response = productionPresentationMapper.toLaborWorksResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productionOrderId}/labor/{laborWorkId}")
    public ResponseEntity<GetProductionLaborWorkResponse> getLaborWork(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID laborWorkId
    ) {
        GetProductionLaborWorkQuery query =
                productionPresentationMapper.toLaborWorkQuery(productionOrderId, laborWorkId);
        GetProductionLaborWorkResult result = getProductionLaborWorkUseCase.execute(query);
        GetProductionLaborWorkResponse response = productionPresentationMapper.toLaborWorkResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/labor/{laborWorkId}/pay")
    public ResponseEntity<PayProductionLaborWorkResponse> payLaborWork(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID laborWorkId,
            @RequestBody(required = false) PayProductionLaborWorkRequest request
    ) {
        PayProductionLaborWorkCommand command = productionPresentationMapper.toPayLaborWorkCommand(
                productionOrderId,
                laborWorkId,
                request
        );
        PayProductionLaborWorkResult result = payProductionLaborWorkUseCase.execute(command);
        PayProductionLaborWorkResponse response = productionPresentationMapper.toPayLaborWorkResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/labor/{laborWorkId}/cancel")
    public ResponseEntity<CancelProductionLaborWorkResponse> cancelLaborWork(
            @PathVariable UUID productionOrderId,
            @PathVariable UUID laborWorkId
    ) {
        CancelProductionLaborWorkCommand command =
                productionPresentationMapper.toCancelLaborWorkCommand(productionOrderId, laborWorkId);
        CancelProductionLaborWorkResult result = cancelProductionLaborWorkUseCase.execute(command);
        CancelProductionLaborWorkResponse response =
                productionPresentationMapper.toCancelLaborWorkResponse(result);

        return ResponseEntity.ok(response);
    }

    private static String contentDisposition(String filename) {
        return "attachment; filename=\"" + filename + "\"";
    }

    private static byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to read the reference image file");
        }
    }
}
