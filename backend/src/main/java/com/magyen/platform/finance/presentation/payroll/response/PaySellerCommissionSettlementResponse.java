package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Liquidación pagada de comisión de vendedor.
 */
public record PaySellerCommissionSettlementResponse(
        UUID settlementId,
        UUID employeeId,
        String displayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal salesSnapshot,
        int orderCountSnapshot,
        BigDecimal commissionSnapshot,
        String status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId,
        String observation
) {
}
