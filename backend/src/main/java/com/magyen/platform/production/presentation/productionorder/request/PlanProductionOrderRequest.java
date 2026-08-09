package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;

/**
 * Payload HTTP para planificar una Orden de Producción.
 * <p>
 * El estado resultante no lo define el cliente; lo determina el dominio.
 */
public record PlanProductionOrderRequest(
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String priority
) {
}
