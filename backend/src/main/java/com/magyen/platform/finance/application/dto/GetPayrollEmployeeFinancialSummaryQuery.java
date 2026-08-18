package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta de resumen financiero de un empleado. Fechas nulas = historial acumulado.
 */
public record GetPayrollEmployeeFinancialSummaryQuery(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
