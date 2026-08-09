package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación de una Orden de Producción para casos de uso de consulta de listado.
 */
public record ProductionOrderResult(
        UUID productionOrderId,
        UUID orderId,
        LocalDate creationDate,
        ProductionStatus status,
        ProductionPriority priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
