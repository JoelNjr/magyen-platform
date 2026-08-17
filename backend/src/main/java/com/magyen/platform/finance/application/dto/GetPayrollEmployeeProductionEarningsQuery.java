package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta de acumulación de mano de obra de un empleado de nómina.
 */
public record GetPayrollEmployeeProductionEarningsQuery(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
