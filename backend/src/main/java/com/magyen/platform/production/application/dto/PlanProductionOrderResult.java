package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del caso de uso de planificación de una Orden de Producción.
 */
public record PlanProductionOrderResult(
        UUID productionOrderId,
        ProductionStatus status,
        ProductionPriority priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate
) {
}
