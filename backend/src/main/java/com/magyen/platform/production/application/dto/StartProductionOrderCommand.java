package com.magyen.platform.production.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para iniciar una Orden de Producción.
 */
public record StartProductionOrderCommand(
        UUID productionOrderId,
        LocalDate actualStartDate
) {
}
