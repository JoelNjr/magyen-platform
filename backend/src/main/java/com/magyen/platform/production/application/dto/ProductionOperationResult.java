package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionOperationStatus;
import com.magyen.platform.production.domain.ProductionOperationType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación de una operación de producción para casos de uso de consulta.
 */
public record ProductionOperationResult(
        UUID operationId,
        ProductionOperationType type,
        ProductionOperationStatus status,
        String assignedOperator,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate actualStartDate,
        LocalDate actualEndDate,
        String observations
) {
}
