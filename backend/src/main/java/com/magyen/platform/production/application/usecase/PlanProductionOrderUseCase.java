package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderResult;
import com.magyen.platform.production.application.port.ProductionCommercialChronologyPort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la planificación de una Orden de Producción existente.
 * <p>
 * Transición de dominio: CREATED → PLANNED.
 * Permite fechas históricas y valida cronología comercial cuando la orden existe.
 */
public class PlanProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionCommercialChronologyPort productionCommercialChronologyPort;

    public PlanProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionCommercialChronologyPort productionCommercialChronologyPort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionCommercialChronologyPort = Objects.requireNonNull(
                productionCommercialChronologyPort,
                "Production commercial chronology port must not be null"
        );
    }

    public PlanProductionOrderResult execute(PlanProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        productionCommercialChronologyPort.findChronology(productionOrder.getOrderId())
                .ifPresent(chronology -> ProductionBusinessChronology.validatePlan(
                        chronology,
                        command.plannedStartDate(),
                        command.plannedEndDate()
                ));

        productionOrder.plan(
                command.plannedStartDate(),
                command.plannedEndDate(),
                command.priority()
        );

        ProductionOrder savedProductionOrder = productionOrderRepository.save(productionOrder);

        return new PlanProductionOrderResult(
                savedProductionOrder.getId(),
                savedProductionOrder.getStatus(),
                savedProductionOrder.getPriority(),
                savedProductionOrder.getPlannedStartDate(),
                savedProductionOrder.getPlannedEndDate()
        );
    }

    private void validateCommand(PlanProductionOrderCommand command) {
        if (command.productionOrderId() == null) {
            throw new IllegalArgumentException("Production order id must not be null");
        }
        if (command.plannedStartDate() == null) {
            throw new IllegalArgumentException("Planned start date must not be null");
        }
        if (command.plannedEndDate() == null) {
            throw new IllegalArgumentException("Planned end date must not be null");
        }
        if (command.priority() == null) {
            throw new IllegalArgumentException("Priority must not be null");
        }
    }
}
