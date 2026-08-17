package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Representación de un operario de producción para casos de uso de consulta.
 */
public record ProductionOperatorResult(
        UUID operatorId,
        String name,
        boolean active
) {
}
