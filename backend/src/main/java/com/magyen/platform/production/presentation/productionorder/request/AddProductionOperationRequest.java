package com.magyen.platform.production.presentation.productionorder.request;

import java.time.LocalDate;

/**
 * Payload HTTP para agregar una operación a una Orden de Producción.
 */
public record AddProductionOperationRequest(
        String type,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
