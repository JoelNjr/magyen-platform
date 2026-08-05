package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para crear una Orden de Producción a partir de una Orden comercial.
 */
public record CreateProductionOrderRequest(
        UUID orderId,
        String priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
