package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationResult;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOperationStatus;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina el inicio de una operación de producción existente.
 */
public class StartProductionOperationUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public StartProductionOperationUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public StartProductionOperationResult execute(StartProductionOperationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionOrder.startOperation(command.operationId());

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new StartProductionOperationResult(
                savedProductionOrder.getId(),
                command.operationId(),
                resolveOperationStatus(savedProductionOrder, command.operationId())
        );
    }

    private void validateCommand(StartProductionOperationCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(command.operationId(), "Operation id must not be null");
    }

    private ProductionOperationStatus resolveOperationStatus(ProductionOrder productionOrder, UUID operationId) {
        return productionOrder.getOperations().stream()
                .filter(operation -> operation.getId().equals(operationId))
                .map(ProductionOperation::getStatus)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production operation not found: " + operationId
                ));
    }
}
