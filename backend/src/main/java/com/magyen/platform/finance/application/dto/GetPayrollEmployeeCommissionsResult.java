package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comisión analítica de un empleado. No es un gasto de Finanzas ni liquidación de nómina.
 */
public record GetPayrollEmployeeCommissionsResult(
        UUID employeeId,
        String displayName,
        PayrollCompensationType compensationType,
        boolean sellerCommissionApplicable,
        boolean active,
        boolean eligibleForNewQuotations,
        LocalDate fromDate,
        LocalDate toDate,
        int numberOfEligibleOrders,
        BigDecimal totalSales,
        BigDecimal commissionRate,
        BigDecimal accumulatedCommission
) {
}
