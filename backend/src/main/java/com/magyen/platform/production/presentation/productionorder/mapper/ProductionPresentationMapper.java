package com.magyen.platform.production.presentation.productionorder.mapper;

import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.AddProductionOperationResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionResult;
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
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.application.dto.ProductionOperationResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionProductSpecificationResult;
import com.magyen.platform.production.application.dto.ProductionSizeBreakdownResult;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationResult;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOperationType;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.presentation.productionorder.request.AddProductionOperationRequest;
import com.magyen.platform.production.presentation.productionorder.request.AssignProductionOperationOperatorRequest;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.PlanProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.request.RegisterProductionMaterialConsumptionRequest;
import com.magyen.platform.production.presentation.productionorder.response.AddProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.AssignProductionOperationOperatorResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.CompleteProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionMaterialConsumptionResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionMaterialConsumptionsResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrdersResponse;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrdersResponse.ProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.PlanProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.ProductionItemResponse;
import com.magyen.platform.production.presentation.productionorder.response.ProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.ProductionProductSpecificationResponse;
import com.magyen.platform.production.presentation.productionorder.response.ProductionSizeBreakdownResponse;
import com.magyen.platform.production.presentation.productionorder.response.RegisterProductionMaterialConsumptionResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOperationResponse;
import com.magyen.platform.production.presentation.productionorder.response.StartProductionOrderResponse;

import java.util.List;
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

    public GetProductionOrderCommand toGetProductionOrderCommand(UUID productionOrderId) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        return new GetProductionOrderCommand(productionOrderId);
    }

    public GetProductionOrdersResponse toResponse(GetProductionOrdersResult result) {
        Objects.requireNonNull(result, "GetProductionOrdersResult must not be null");

        List<ProductionOrderResponse> productionOrders = result.productionOrders().stream()
                .map(this::toProductionOrderResponse)
                .toList();

        return new GetProductionOrdersResponse(productionOrders);
    }

    public GetProductionOrderResponse toResponse(GetProductionOrderResult result) {
        Objects.requireNonNull(result, "GetProductionOrderResult must not be null");

        List<ProductionItemResponse> items = result.items() == null
                ? List.of()
                : result.items().stream()
                        .map(this::toProductionItemResponse)
                        .toList();

        List<ProductionOperationResponse> operations = result.operations().stream()
                .map(this::toProductionOperationResponse)
                .toList();

        return new GetProductionOrderResponse(
                result.productionOrderId(),
                result.orderId(),
                result.creationDate(),
                result.status().name(),
                result.priority().name(),
                result.plannedStartDate(),
                result.plannedEndDate(),
                result.observations(),
                items,
                operations
        );
    }

    public PlanProductionOrderCommand toPlanCommand(
            UUID productionOrderId,
            PlanProductionOrderRequest request
    ) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(request, "PlanProductionOrderRequest must not be null");

        return new PlanProductionOrderCommand(
                productionOrderId,
                request.plannedStartDate(),
                request.plannedEndDate(),
                resolvePriority(request.priority())
        );
    }

    public PlanProductionOrderResponse toPlanResponse(PlanProductionOrderResult result) {
        Objects.requireNonNull(result, "PlanProductionOrderResult must not be null");

        return new PlanProductionOrderResponse(
                result.productionOrderId(),
                result.status().name(),
                result.priority().name(),
                result.plannedStartDate(),
                result.plannedEndDate()
        );
    }

    public StartProductionOrderCommand toStartOrderCommand(UUID productionOrderId) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        return new StartProductionOrderCommand(productionOrderId);
    }

    public StartProductionOrderResponse toStartOrderResponse(StartProductionOrderResult result) {
        Objects.requireNonNull(result, "StartProductionOrderResult must not be null");

        return new StartProductionOrderResponse(
                result.productionOrderId(),
                result.status().name()
        );
    }

    public CompleteProductionOrderCommand toCompleteOrderCommand(UUID productionOrderId) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        return new CompleteProductionOrderCommand(productionOrderId);
    }

    public CompleteProductionOrderResponse toCompleteOrderResponse(CompleteProductionOrderResult result) {
        Objects.requireNonNull(result, "CompleteProductionOrderResult must not be null");

        return new CompleteProductionOrderResponse(
                result.productionOrderId(),
                result.status().name()
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

    private ProductionOrderResponse toProductionOrderResponse(ProductionOrderResult result) {
        return new ProductionOrderResponse(
                result.productionOrderId(),
                result.orderId(),
                result.creationDate(),
                result.status().name(),
                result.priority().name(),
                result.plannedStartDate(),
                result.plannedEndDate(),
                result.observations()
        );
    }

    private ProductionItemResponse toProductionItemResponse(ProductionItemResult result) {
        Objects.requireNonNull(result, "Production item result must not be null");

        List<ProductionSizeBreakdownResponse> sizes = result.sizes() == null
                ? List.of()
                : result.sizes().stream()
                        .map(this::toProductionSizeBreakdownResponse)
                        .toList();

        return new ProductionItemResponse(
                result.productionItemId(),
                result.productName(),
                result.quantity(),
                toProductionProductSpecificationResponse(result.productSpecification()),
                sizes
        );
    }

    private ProductionProductSpecificationResponse toProductionProductSpecificationResponse(
            ProductionProductSpecificationResult result
    ) {
        if (result == null) {
            return new ProductionProductSpecificationResponse(
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    false,
                    null,
                    false,
                    false,
                    false,
                    null,
                    null
            );
        }

        return new ProductionProductSpecificationResponse(
                result.garmentType(),
                result.collarType(),
                result.sleeveType(),
                result.garmentVariant(),
                result.sublimationRequired(),
                result.embroideryRequired(),
                result.dtfRequired(),
                result.decorationNotes(),
                result.includesNames(),
                result.includesNumbers(),
                result.includesLogos(),
                result.personalizationNotes(),
                result.itemObservations()
        );
    }

    private ProductionSizeBreakdownResponse toProductionSizeBreakdownResponse(
            ProductionSizeBreakdownResult result
    ) {
        Objects.requireNonNull(result, "Production size breakdown result must not be null");

        return new ProductionSizeBreakdownResponse(
                result.size(),
                result.quantity()
        );
    }

    private ProductionOperationResponse toProductionOperationResponse(ProductionOperationResult result) {
        return new ProductionOperationResponse(
                result.operationId(),
                result.type().name(),
                result.status().name(),
                result.assignedOperator(),
                result.plannedStartDate(),
                result.plannedEndDate(),
                result.actualStartDate(),
                result.actualEndDate(),
                result.observations()
        );
    }

    private ProductionPriority resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return ProductionPriority.NORMAL;
        }

        return ProductionPriority.valueOf(priority);
    }

    public RegisterProductionMaterialConsumptionCommand toRegisterMaterialConsumptionCommand(
            UUID productionOrderId,
            RegisterProductionMaterialConsumptionRequest request
    ) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        Objects.requireNonNull(request, "RegisterProductionMaterialConsumptionRequest must not be null");

        return new RegisterProductionMaterialConsumptionCommand(
                productionOrderId,
                request.inventoryItemId(),
                request.quantity(),
                request.unitOfMeasure(),
                request.observation()
        );
    }

    public RegisterProductionMaterialConsumptionResponse toRegisterMaterialConsumptionResponse(
            RegisterProductionMaterialConsumptionResult result
    ) {
        Objects.requireNonNull(result, "RegisterProductionMaterialConsumptionResult must not be null");

        return new RegisterProductionMaterialConsumptionResponse(
                result.consumptionId(),
                result.productionOrderId(),
                result.inventoryItemId(),
                result.quantity(),
                result.unitOfMeasure(),
                result.consumptionDate(),
                result.observation()
        );
    }

    public GetProductionMaterialConsumptionsQuery toMaterialConsumptionsQuery(UUID productionOrderId) {
        Objects.requireNonNull(productionOrderId, "Production order id must not be null");
        return new GetProductionMaterialConsumptionsQuery(productionOrderId);
    }

    public GetProductionMaterialConsumptionsResponse toResponse(GetProductionMaterialConsumptionsResult result) {
        Objects.requireNonNull(result, "GetProductionMaterialConsumptionsResult must not be null");

        return new GetProductionMaterialConsumptionsResponse(
                result.consumptions().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    public GetProductionMaterialConsumptionResponse toResponse(GetProductionMaterialConsumptionResult result) {
        Objects.requireNonNull(result, "GetProductionMaterialConsumptionResult must not be null");

        return new GetProductionMaterialConsumptionResponse(
                result.consumptionId(),
                result.productionOrderId(),
                result.inventoryItemId(),
                result.quantity(),
                result.unitOfMeasure(),
                result.consumptionDate(),
                result.observation()
        );
    }
}
