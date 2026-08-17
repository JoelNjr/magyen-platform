package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderResult;
import com.magyen.platform.production.application.port.ProductionCommercialChronologyPort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que coordina la finalización de una Orden de Producción existente.
 * <p>
 * Transición de dominio: IN_PROGRESS → COMPLETED.
 * Conserva la fecha real de cierre, incluyendo fechas históricas.
 */
public class CompleteProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionCommercialChronologyPort productionCommercialChronologyPort;

    public CompleteProductionOrderUseCase(
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

    public CompleteProductionOrderResult execute(CompleteProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        LocalDate actualCompletionDate = command.actualCompletionDate() != null
                ? command.actualCompletionDate()
                : LocalDate.now();

        productionCommercialChronologyPort.findChronology(productionOrder.getOrderId())
                .ifPresent(chronology -> ProductionBusinessChronology.validateCompletion(
                        chronology,
                        productionOrder.getActualStartDate(),
                        actualCompletionDate
                ));

        productionOrder.complete(actualCompletionDate);

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
