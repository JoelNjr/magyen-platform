package com.magyen.platform.production.presentation.productionorder.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP tras planificar una Orden de Producción exitosamente.
 */
public record PlanProductionOrderResponse(
        UUID productionOrderId,
        String status,
        String priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate
) {
}
