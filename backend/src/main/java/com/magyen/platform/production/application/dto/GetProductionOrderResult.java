package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Resultado del caso de uso que consulta una Orden de Producción completa.
 */
public record GetProductionOrderResult(
        UUID productionOrderId,
        UUID orderId,
        LocalDate creationDate,
        ProductionStatus status,
        ProductionPriority priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations,
        List<ProductionItemResult> items,
        List<ProductionOperationResult> operations
) {
}
