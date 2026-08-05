package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para iniciar una operación de producción.
 */
public record StartProductionOperationCommand(
        UUID productionOrderId,
        UUID operationId
) {
}
