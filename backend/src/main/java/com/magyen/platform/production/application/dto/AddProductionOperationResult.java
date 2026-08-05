package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionOperationType;

import java.util.UUID;

/**
 * Resultado del caso de uso de agregar una operación a una Orden de Producción.
 */
public record AddProductionOperationResult(
        UUID productionOrderId,
        UUID operationId,
        ProductionOperationType operationType
) {
}
