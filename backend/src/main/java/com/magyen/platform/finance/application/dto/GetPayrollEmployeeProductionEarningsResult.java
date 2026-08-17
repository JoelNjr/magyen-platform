package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen de mano de obra de un empleado. No aplica a nómina fija.
 */
public record GetPayrollEmployeeProductionEarningsResult(
        UUID employeeId,
        String displayName,
        PayrollCompensationType compensationType,
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
