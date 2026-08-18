package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta analítica de comisión de un empleado vendedor.
 */
public record GetPayrollEmployeeCommissionsQuery(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
