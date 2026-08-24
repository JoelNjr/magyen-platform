package com.magyen.platform.production.application.dto;

import java.time.LocalDate;

/**
 * Línea de operación para el PDF operativo de orden de producción.
 * <p>
 * No incluye identidades técnicas.
 */
public record ProductionDocumentOperationLine(
        String typeLabel,
        String statusLabel,
        String assignedOperator,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
