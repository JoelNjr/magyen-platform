package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionPriority;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para crear una Orden de Producción a partir de una Orden comercial.
 */
public record CreateProductionOrderCommand(
        UUID orderId,
        ProductionPriority priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations
) {
}
