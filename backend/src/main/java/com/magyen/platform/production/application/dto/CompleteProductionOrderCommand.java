package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para completar una Orden de Producción.
 */
public record CompleteProductionOrderCommand(
        UUID productionOrderId
) {
}
