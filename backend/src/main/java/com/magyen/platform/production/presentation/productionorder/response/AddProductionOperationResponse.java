package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras agregar una operación a una Orden de Producción exitosamente.
 */
public record AddProductionOperationResponse(
        UUID productionOrderId,
        UUID operationId,
        String operationType
) {
}
