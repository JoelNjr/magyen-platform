package com.magyen.platform.production.application.port;

import java.util.UUID;

/**
 * Operario elegible para mano de obra por producción (vista de Application).
 */
public record ProductionLaborOperatorInfo(
        UUID employeeId,
        String displayName
) {
}
