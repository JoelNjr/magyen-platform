package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;

/**
 * Consulta de desempeño de vendedores FIXED_PAYROLL (activos e inactivos).
 */
public record GetPayrollEmployeePerformanceQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
