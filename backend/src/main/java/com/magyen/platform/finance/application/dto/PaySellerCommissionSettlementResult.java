package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.SellerCommissionSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del pago de una comisión mensual de vendedor.
 */
public record PaySellerCommissionSettlementResult(
        UUID settlementId,
        UUID employeeId,
        String displayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal salesSnapshot,
        int orderCountSnapshot,
        BigDecimal commissionSnapshot,
        SellerCommissionSettlementStatus status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId,
        String observation
) {
}
