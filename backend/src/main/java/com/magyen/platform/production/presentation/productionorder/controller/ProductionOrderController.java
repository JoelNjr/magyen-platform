package com.magyen.platform.production.presentation.productionorder.controller;

import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.AddProductionOperationResult;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorCommand;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorResult;
import com.magyen.platform.production.application.dto.CompleteProductionOperationCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOperationResult;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationResult;
import com.magyen.platform.production.application.usecase.AddProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.AssignProductionOperationOperatorUseCase;
import com.magyen.platform.production.application.usecase.CompleteProductionOperationUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOperationUseCase;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import com.magyen.platform.production.presentation.productionorder.request.AddProductionOperationRequest;
import com.magyen.platform.production.presentation.productionorder.request.AssignProductionOperationOperatorRequest;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.response.AddProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.AssignProductionOperationOperatorResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOperationResponse;
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
 * Expone la API REST de órdenes de producción.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/production-orders")
public class ProductionOrderController {

    private final CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;
    private final AddProductionOperationUseCase addProductionOperationUseCase;
    private final AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase;
    private final StartProductionOperationUseCase startProductionOperationUseCase;
    private final CompleteProductionOperationUseCase completeProductionOperationUseCase;
    private final ProductionPresentationMapper productionPresentationMapper;

    public ProductionOrderController(
            CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase,
            AddProductionOperationUseCase addProductionOperationUseCase,
            AssignProductionOperationOperatorUseCase assignProductionOperationOperatorUseCase,
            StartProductionOperationUseCase startProductionOperationUseCase,
            CompleteProductionOperationUseCase completeProductionOperationUseCase,
            ProductionPresentationMapper productionPresentationMapper
    ) {
        this.createProductionOrderFromOrderUseCase = createProductionOrderFromOrderUseCase;
        this.addProductionOperationUseCase = addProductionOperationUseCase;
        this.assignProductionOperationOperatorUseCase = assignProductionOperationOperatorUseCase;
        this.startProductionOperationUseCase = startProductionOperationUseCase;
        this.completeProductionOperationUseCase = completeProductionOperationUseCase;
        this.productionPresentationMapper = productionPresentationMapper;
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
