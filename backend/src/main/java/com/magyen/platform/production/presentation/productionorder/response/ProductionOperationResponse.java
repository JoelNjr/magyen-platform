package com.magyen.platform.production.presentation.productionorder.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Operación de producción expuesta por la API de consulta.
 */
public record ProductionOperationResponse(
        UUID operationId,
        String type,
        String status,
        String assignedOperator,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate actualStartDate,
        LocalDate actualEndDate,
        String observations
) {
}
