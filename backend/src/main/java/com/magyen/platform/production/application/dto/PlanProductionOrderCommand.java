package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para planificar una Orden de Producción.
 */
public record PlanProductionOrderCommand(
        UUID productionOrderId,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        ProductionPriority priority
) {
}
