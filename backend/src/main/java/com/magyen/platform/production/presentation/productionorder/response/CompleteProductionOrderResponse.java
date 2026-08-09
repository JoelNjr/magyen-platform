package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras completar una Orden de Producción exitosamente.
 */
public record CompleteProductionOrderResponse(
        UUID productionOrderId,
        String status
) {
}
