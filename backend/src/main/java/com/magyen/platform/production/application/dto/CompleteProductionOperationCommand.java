package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para completar una operación de producción.
 */
public record CompleteProductionOperationCommand(
        UUID productionOrderId,
        UUID operationId
) {
}
