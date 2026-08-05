package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras asignar un operador a una operación de producción exitosamente.
 */
public record AssignProductionOperationOperatorResponse(
        UUID productionOrderId,
        UUID operationId,
        String assignedOperator
) {
}
