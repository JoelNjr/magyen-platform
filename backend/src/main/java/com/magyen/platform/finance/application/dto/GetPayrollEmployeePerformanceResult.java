package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Desempeño analítico de vendedores. Incluye empleados FIXED_PAYROLL inactivos.
 */
public record GetPayrollEmployeePerformanceResult(
        List<GetPayrollEmployeeCommissionsResult> sellers
) {
}
