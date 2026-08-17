package com.magyen.platform.production.presentation.operator.response;

import java.util.UUID;

/**
 * Representación HTTP de un operario de producción.
 */
public record ProductionOperatorResponse(
        UUID operatorId,
        String name,
        boolean active
) {
}
