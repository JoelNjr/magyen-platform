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
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderResult;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderResult;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import com.magyen.platform.production.presentation.productionorder.request.AddProductionOperationRequest;
import com.magyen.platform.production.presentation.productionorder.request.AssignProductionOperationOperatorRequest;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.PlanProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.response.AddProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.AssignProductionOperationOperatorResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrdersResponse;
import com.magyen.platform.production.presentation.productionorder.response.PlanProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final PlanProductionOrderUseCase planProductionOrderUseCase;
    private final StartProductionOrderUseCase startProductionOrderUseCase;
    private final CompleteProductionOrderUseCase completeProductionOrderUseCase;
    private final AddProductionOperationUseCase addProductionOperationUseCase;
    private final AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase;
    private final StartProductionOperationUseCase startProductionOperationUseCase;
    private final CompleteProductionOperationUseCase completeProductionOperationUseCase;
    private final ProductionPresentationMapper productionPresentationMapper;

    public ProductionOrderController(
            CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase,
            GetProductionOrdersUseCase getProductionOrdersUseCase,
            GetProductionOrderUseCase getProductionOrderUseCase,
            PlanProductionOrderUseCase planProductionOrderUseCase,
            StartProductionOrderUseCase startProductionOrderUseCase,
            CompleteProductionOrderUseCase completeProductionOrderUseCase,
            AddProductionOperationUseCase addProductionOperationUseCase,
            AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase,
            StartProductionOperationUseCase startProductionOperationUseCase,
            CompleteProductionOperationUseCase completeProductionOperationUseCase,
            ProductionPresentationMapper productionPresentationMapper
    ) {
        this.createProductionOrderFromOrderUseCase = createProductionOrderFromOrderUseCase;
        this.getProductionOrdersUseCase = getProductionOrdersUseCase;
        this.getProductionOrderUseCase = getProductionOrderUseCase;
        this.planProductionOrderUseCase = planProductionOrderUseCase;
        this.startProductionOrderUseCase = startProductionOrderUseCase;
        this.completeProductionOrderUseCase = completeProductionOrderUseCase;
        this.addProductionOperationUseCase = addProductionOperationUseCase;
        this.assignProductionOperationOperatorUseCase = assignProductionOperationOperatorUseCase;
        this.startProductionOperationUseCase = startProductionOperationUseCase;
        this.completeProductionOperationUseCase = completeProductionOperationUseCase;
        this.productionPresentationMapper = productionPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetProductionOrdersResponse> getProductionOrders() {
        GetProductionOrdersResult result = getProductionOrdersUseCase.execute();
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
            @PathVariable UUID productionOrderId
    ) {
        StartProductionOrderCommand command = productionPresentationMapper.toStartOrderCommand(productionOrderId);
        StartProductionOrderResult result = startProductionOrderUseCase.execute(command);
        StartProductionOrderResponse response = productionPresentationMapper.toStartOrderResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productionOrderId}/complete")
    public ResponseEntity<CompleteProductionOrderResponse> completeProductionOrder(
            @PathVariable UUID productionOrderId
    ) {
        CompleteProductionOrderCommand command = productionPresentationMapper.toCompleteOrderCommand(productionOrderId);
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
}
