package com.magyen.platform.production.presentation.labor.response;

import java.util.UUID;

/**
 * Operario elegible para el selector de mano de obra.
 */
public record ProductionLaborOperatorResponse(
        UUID employeeId,
        String displayName
) {
}
