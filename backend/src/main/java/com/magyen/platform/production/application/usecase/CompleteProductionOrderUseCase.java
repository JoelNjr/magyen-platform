package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la finalización de una Orden de Producción existente.
 * <p>
 * Transición de dominio: IN_PROGRESS → COMPLETED.
 * El agregado exige que todas las operaciones estén completadas.
 */
public class CompleteProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public CompleteProductionOrderUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public CompleteProductionOrderResult execute(CompleteProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionOrder.complete();

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new CompleteProductionOrderResult(
                savedProductionOrder.getId(),
                savedProductionOrder.getStatus()
        );
    }

    private void validateCommand(CompleteProductionOrderCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
    }
}
