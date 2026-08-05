package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorCommand;
import com.magyen.platform.production.application.dto.AssignProductionOperationOperatorResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la asignación de un operador a una operación de producción existente.
 */
public class AssignProductionOperationOperatorUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public AssignProductionOperationOperatorUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public AssignProductionOperationOperatorResult execute(AssignProductionOperationOperatorCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionOrder.assignOperator(command.operationId(), command.assignedOperator());

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new AssignProductionOperationOperatorResult(
                savedProductionOrder.getId(),
                command.operationId(),
                command.assignedOperator()
        );
    }

    private void validateCommand(AssignProductionOperationOperatorCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(command.operationId(), "Operation id must not be null");
    }
}
