package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Comando para cancelar un registro de mano de obra PENDING.
 */
public record CancelProductionLaborWorkCommand(
        UUID productionOrderId,
        UUID laborWorkId
) {
}
