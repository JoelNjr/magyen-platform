package com.magyen.platform.finance.presentation.payroll.response;

import java.util.List;

/**
 * Listado HTTP de desempeño analítico de vendedores.
 */
public record PayrollEmployeePerformanceResponse(
        List<PayrollEmployeeCommissionsResponse> sellers
) {
}
