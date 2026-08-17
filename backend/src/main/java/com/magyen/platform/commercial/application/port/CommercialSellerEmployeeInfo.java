package com.magyen.platform.commercial.application.port;

import java.util.UUID;

/**
 * Empleado de Finance elegible o resuelto como vendedor comercial.
 */
public record CommercialSellerEmployeeInfo(
        UUID employeeId,
        String displayName,
        boolean active
) {
}
