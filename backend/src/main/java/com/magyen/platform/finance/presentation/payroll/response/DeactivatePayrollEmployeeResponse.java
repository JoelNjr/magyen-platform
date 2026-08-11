package com.magyen.platform.finance.presentation.payroll.response;

import java.util.UUID;

/**
 * Respuesta HTTP de desactivación de empleado de nómina.
 */
public record DeactivatePayrollEmployeeResponse(
        UUID employeeId,
        boolean active
) {
}
