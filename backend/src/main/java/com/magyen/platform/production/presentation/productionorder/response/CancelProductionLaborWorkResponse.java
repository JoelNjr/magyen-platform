package com.magyen.platform.production.presentation.productionorder.response;

import java.util.UUID;

/**
 * Respuesta HTTP de la cancelación de mano de obra.
 */
public record CancelProductionLaborWorkResponse(
        UUID laborWorkId,
        UUID productionOrderId,
        String status
) {
}
