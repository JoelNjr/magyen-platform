package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionOperationType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para agregar una operación a una Orden de Producción.
 */
public record AddProductionOperationCommand(
        UUID productionOrderId,
        ProductionOperationType type,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
