package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para asignar un operador a una operación de producción.
 */
public record AssignProductionOperationOperatorCommand(
        UUID productionOrderId,
        UUID operationId,
        String assignedOperator
) {
}
