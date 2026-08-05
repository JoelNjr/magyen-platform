package com.magyen.platform.production.presentation.productionorder.request;

/**
 * Payload HTTP para asignar un operador a una operación de producción.
 */
public record AssignProductionOperationOperatorRequest(
        String assignedOperator
) {
}
