package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Resultado del caso de uso de asignación de operador a una operación de producción.
 */
public record AssignProductionOperationOperatorResult(
        UUID productionOrderId,
        UUID operationId,
        String assignedOperator
) {
}
