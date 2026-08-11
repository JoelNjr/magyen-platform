package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de un empleado de nómina por identidad.
 */
public record GetPayrollEmployeeQuery(
        UUID employeeId
) {
}
