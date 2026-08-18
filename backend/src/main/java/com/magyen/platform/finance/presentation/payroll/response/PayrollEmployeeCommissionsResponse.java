package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comisión analítica HTTP de un empleado vendedor.
 */
public record PayrollEmployeeCommissionsResponse(
        UUID employeeId,
        String displayName,
        String compensationType,
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
