package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de elegibilidad/lectura de un operario para mano de obra por producción.
 */
public record ResolveProductionLaborOperatorQuery(
        UUID employeeId
) {
}
