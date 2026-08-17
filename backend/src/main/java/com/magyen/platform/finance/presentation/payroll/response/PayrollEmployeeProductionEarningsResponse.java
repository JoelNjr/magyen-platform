package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen HTTP de mano de obra de un empleado.
 */
public record PayrollEmployeeProductionEarningsResponse(
        UUID employeeId,
        String displayName,
        String compensationType,
        boolean productionLaborApplicable,
        LocalDate fromDate,
        LocalDate toDate,
        int laborWorkCount,
        BigDecimal totalQuantity,
        BigDecimal totalCalculatedAmount,
        BigDecimal totalPaidAmount,
        BigDecimal totalPendingAmount
) {
}
