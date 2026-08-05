package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras completar una operación de producción exitosamente.
 */
public record CompleteProductionOperationResponse(
        UUID productionOrderId,
        UUID operationId,
        String operationStatus
) {
}
