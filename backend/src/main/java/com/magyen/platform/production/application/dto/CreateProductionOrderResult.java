package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del caso de uso de creación de Orden de Producción desde Orden comercial.
 */
public record CreateProductionOrderResult(
        UUID productionOrderId,
        UUID orderId,
        ProductionStatus status,
        ProductionPriority priority,
        LocalDate creationDate
) {
}
