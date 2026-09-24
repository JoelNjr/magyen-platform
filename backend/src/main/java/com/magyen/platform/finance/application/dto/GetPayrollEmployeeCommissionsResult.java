package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.CommissionOrderLine;
import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comisión mensual de un empleado. No es un gasto de Finanzas ni una liquidación.
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
        BigDecimal accumulatedCommission,
        String settlementStatus,
        List<CommissionOrderLine> orders,
        UUID settlementId,
        BigDecimal paidSalesSnapshot,
        Integer paidOrderCountSnapshot,
        BigDecimal paidCommissionSnapshot,
        LocalDate actualPaymentDate,
        UUID financialTransactionId
) {
}
