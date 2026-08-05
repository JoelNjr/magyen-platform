package com.magyen.platform.production.presentation.productionorder.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP tras crear una Orden de Producción exitosamente.
 */
public record CreateProductionOrderResponse(
        UUID productionOrderId,
        UUID orderId,
        String status,
        String priority,
        LocalDate creationDate
) {
}
