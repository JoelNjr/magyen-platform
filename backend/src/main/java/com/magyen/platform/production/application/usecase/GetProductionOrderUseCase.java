package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionOperationResult;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta una Orden de Producción completa por identificador.
 */
public class GetProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public GetProductionOrderUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public GetProductionOrderResult execute(GetProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        return toResult(productionOrder);
    }

    private void validateCommand(GetProductionOrderCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
    }

    private GetProductionOrderResult toResult(ProductionOrder productionOrder) {
        List<ProductionOperationResult> operations = productionOrder.getOperations().stream()
                .map(this::toOperationResult)
                .toList();

        return new GetProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getObservations(),
                operations
        );
    }

    private ProductionOperationResult toOperationResult(ProductionOperation operation) {
        return new ProductionOperationResult(
                operation.getId(),
                operation.getType(),
                operation.getStatus(),
                operation.getAssignedOperator(),
                operation.getPlannedStartDate(),
                operation.getPlannedEndDate(),
                operation.getActualStartDate(),
                operation.getActualEndDate(),
                operation.getObservations()
        );
    }
}
