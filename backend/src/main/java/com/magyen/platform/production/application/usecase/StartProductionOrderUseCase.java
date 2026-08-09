package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina el inicio de una Orden de Producción existente.
 * <p>
 * Transición de dominio: PLANNED → IN_PROGRESS.
 */
public class StartProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public StartProductionOrderUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public StartProductionOrderResult execute(StartProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionOrder.start();

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new StartProductionOrderResult(
                savedProductionOrder.getId(),
                savedProductionOrder.getStatus()
        );
    }

    private void validateCommand(StartProductionOrderCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
    }
}
