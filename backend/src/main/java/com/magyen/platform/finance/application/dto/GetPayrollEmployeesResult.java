package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado del listado de empleados de nómina.
 */
public record GetPayrollEmployeesResult(
        List<GetPayrollEmployeeResult> employees
) {
}
