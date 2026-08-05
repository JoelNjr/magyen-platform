package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionOperationStatus;

import java.util.UUID;

/**
 * Resultado del caso de uso de inicio de una operación de producción.
 */
public record StartProductionOperationResult(
        UUID productionOrderId,
        UUID operationId,
        ProductionOperationStatus operationStatus
) {
}
