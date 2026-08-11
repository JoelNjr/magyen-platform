package com.magyen.platform.finance.presentation.payroll.response;

import java.util.UUID;

/**
 * Respuesta HTTP de activación de empleado de nómina.
 */
public record ActivatePayrollEmployeeResponse(
        UUID employeeId,
        boolean active
) {
}
