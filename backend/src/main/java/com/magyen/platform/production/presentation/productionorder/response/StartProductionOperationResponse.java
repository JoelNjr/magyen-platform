package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras iniciar una operación de producción exitosamente.
 */
public record StartProductionOperationResponse(
        UUID productionOrderId,
        UUID operationId,
        String operationStatus
) {
}
