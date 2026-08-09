package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras iniciar una Orden de Producción exitosamente.
 */
public record StartProductionOrderResponse(
        UUID productionOrderId,
        String status
) {
}
