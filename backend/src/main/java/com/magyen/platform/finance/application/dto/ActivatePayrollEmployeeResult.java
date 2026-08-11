package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Resultado de activación de un empleado de nómina.
 */
public record ActivatePayrollEmployeeResult(
        UUID employeeId,
        boolean active
) {
}
