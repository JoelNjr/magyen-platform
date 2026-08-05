package com.magyen.platform.production.presentation.productionorder.mapper;

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
import com.magyen.platform.production.domain.ProductionOperationType;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.presentation.productionorder.request.AddProductionOperationRequest;
import com.magyen.platform.production.presentation.productionorder.request.AssignProductionOperationOperatorRequest;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.response.AddProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.AssignProductionOperationOperatorResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOperationResponse;

import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class ProductionPresentationMapper {

    public CreateProductionOrderCommand toCommand(CreateProductionOrderRequest request) {
        Objects.requireNonNull(request, "CreateProductionOrderRequest must not be null");

        return new CreateProductionOrderCommand(
                request.orderId(),
                resolvePriority(request.priority()),
                request.plannedStartDate(),
                request.plannedEndDate(),
                request.observations()
        );
    }

    public CreateProductionOrderResponse toResponse(CreateProductionOrderResult result) {
        Objects.requireNonNull(result, "CreateProductionOrderResult must not be null");

        return new CreateProductionOrderResponse(
                result.productionOrderId(),
                result.orderId(),
                result.status().name(),
                result.priority().name(),
                result.creationDate()
        );
    }

    public AddProductionOperationCommand toAddOperationCommand(
            UUID productionOrderId,
            AddProductionOperationRequest request
    ) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(request, "AddProductionOperationRequest must not be null");

        return new AddProductionOperationCommand(
                productionOrderId,
                ProductionOperationType.valueOf(request.type()),
                request.plannedStartDate(),
                request.plannedEndDate(),
                request.observations()
        );
    }

    public AddProductionOperationResponse toAddOperationResponse(AddProductionOperationResult result) {
        Objects.requireNonNull(result, "AddProductionOperationResult must not be null");

        return new AddProductionOperationResponse(
                result.productionOrderId(),
                result.operationId(),
                result.operationType().name()
        );
    }

    public AssignProductionOperationOperatorCommand toAssignOperatorCommand(
            UUID productionOrderId,
            UUID operationId,
            AssignProductionOperationOperatorRequest request
    ) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(operationId, "Operation id must not be null");
        Objects.requireNonNull(request, "AssignProductionOperationOperatorRequest must not be null");

        return new AssignProductionOperationOperatorCommand(
                productionOrderId,
                operationId,
                request.assignedOperator()
        );
    }

    public AssignProductionOperationOperatorResponse toAssignOperatorResponse(
            AssignProductionOperationOperatorResult result
    ) {
        Objects.requireNonNull(result, "AssignProductionOperationOperatorResult must not be null");

        return new AssignProductionOperationOperatorResponse(
                result.productionOrderId(),
                result.operationId(),
                result.assignedOperator()
        );
    }

    public StartProductionOperationCommand toStartOperationCommand(UUID productionOrderId, UUID operationId) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(operationId, "Operation id must not be null");

        return new StartProductionOperationCommand(productionOrderId, operationId);
    }

    public StartProductionOperationResponse toStartOperationResponse(StartProductionOperationResult result) {
        Objects.requireNonNull(result, "StartProductionOperationResult must not be null");

        return new StartProductionOperationResponse(
                result.productionOrderId(),
                result.operationId(),
                result.operationStatus().name()
        );
    }

    public CompleteProductionOperationCommand toCompleteOperationCommand(
            UUID productionOrderId,
            UUID operationId
    ) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(operationId, "Operation id must not be null");

        return new CompleteProductionOperationCommand(productionOrderId, operationId);
    }

    public CompleteProductionOperationResponse toCompleteOperationResponse(
            CompleteProductionOperationResult result
    ) {
        Objects.requireNonNull(result, "CompleteProductionOperationResult must not be null");

        return new CompleteProductionOperationResponse(
                result.productionOrderId(),
                result.operationId(),
                result.operationStatus().name()
        );
    }

    private ProductionPriority resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return ProductionPriority.NORMAL;
        }

        return ProductionPriority.valueOf(priority);
    }
}
