package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Resultado de desactivación de un empleado de nómina.
 */
public record DeactivatePayrollEmployeeResult(
        UUID employeeId,
        boolean active
) {
}
