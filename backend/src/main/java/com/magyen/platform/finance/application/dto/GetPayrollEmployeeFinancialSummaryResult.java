package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumen de compensación V1. No es liquidación de nómina ni salario neto.
 */
public record GetPayrollEmployeeFinancialSummaryResult(
        UUID employeeId,
        String displayName,
        boolean active,
        PayrollCompensationType compensationType,
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
