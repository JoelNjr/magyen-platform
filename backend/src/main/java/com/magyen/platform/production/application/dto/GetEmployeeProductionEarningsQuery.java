package com.magyen.platform.production.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta de acumulación histórica de mano de obra por empleado de nómina.
 */
public record GetEmployeeProductionEarningsQuery(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
