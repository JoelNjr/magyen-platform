package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comisión mensual HTTP de un empleado. No es un gasto de Finanzas ni una liquidación.
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
        BigDecimal accumulatedCommission,
        String settlementStatus,
        List<PayrollEmployeeCommissionOrderResponse> orders
) {
}
