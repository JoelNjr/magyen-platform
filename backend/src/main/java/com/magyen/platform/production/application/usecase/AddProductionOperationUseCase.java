package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.AddProductionOperationResult;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso que coordina la adición de una operación a una Orden de Producción existente.
 */
public class AddProductionOperationUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public AddProductionOperationUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public AddProductionOperationResult execute(AddProductionOperationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionOrder.addOperation(
                command.type(),
                command.plannedStartDate(),
                command.plannedEndDate(),
                command.observations()
        );

        UUID operationId = lastCreatedOperationId(productionOrder);

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new AddProductionOperationResult(
                savedProductionOrder.getId(),
                operationId,
                command.type()
        );
    }

    private void validateCommand(AddProductionOperationCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
    }

    private UUID lastCreatedOperationId(ProductionOrder productionOrder) {
        List<ProductionOperation> operations = productionOrder.getOperations();
        return operations.get(operations.size() - 1).getId();
    }
}
