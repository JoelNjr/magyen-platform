package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen financiero HTTP de un empleado. No es liquidación de nómina.
 */
public record PayrollEmployeeFinancialSummaryResponse(
        UUID employeeId,
        String displayName,
        boolean active,
        String compensationType,
        BigDecimal fixedAmount,
        boolean sellerCommissionApplicable,
        boolean productionLaborApplicable,
        boolean eligibleForNewQuotations,
        LocalDate fromDate,
        LocalDate toDate,
        int numberOfEligibleOrders,
        BigDecimal totalSales,
        BigDecimal commissionRate,
        BigDecimal accumulatedCommission,
        int laborWorkCount,
        BigDecimal productionGenerated,
        BigDecimal productionPaid,
        BigDecimal productionPending,
        int activeDeductionCount,
        BigDecimal activeDeductionTotal
) {
}
