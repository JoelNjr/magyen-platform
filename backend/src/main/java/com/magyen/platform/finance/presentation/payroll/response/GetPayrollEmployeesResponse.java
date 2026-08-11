package com.magyen.platform.finance.presentation.payroll.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de empleados de nómina.
 */
public record GetPayrollEmployeesResponse(
        List<PayrollEmployeeResponse> employees
) {
}
