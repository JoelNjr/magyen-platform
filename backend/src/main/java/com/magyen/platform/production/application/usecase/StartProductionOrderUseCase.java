package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderResult;
import com.magyen.platform.production.application.port.ProductionCommercialChronologyPort;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Caso de uso que coordina el inicio de una Orden de Producción existente.
 * <p>
 * Transición de dominio: PLANNED → IN_PROGRESS.
 * Conserva la fecha real de inicio, incluyendo fechas históricas.
 */
public class StartProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionCommercialChronologyPort productionCommercialChronologyPort;

    public StartProductionOrderUseCase(
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

    public StartProductionOrderResult execute(StartProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        LocalDate actualStartDate = command.actualStartDate() != null
                ? command.actualStartDate()
                : LocalDate.now();

        productionCommercialChronologyPort.findChronology(productionOrder.getOrderId())
                .ifPresent(chronology -> ProductionBusinessChronology.validateStart(chronology, actualStartDate));

        productionOrder.start(actualStartDate);

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
