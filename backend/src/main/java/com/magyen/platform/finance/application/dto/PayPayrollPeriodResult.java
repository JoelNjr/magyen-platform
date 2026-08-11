package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollPeriodStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del pago de un período, incluyendo el movimiento del ledger generado.
 */
public record PayPayrollPeriodResult(
        UUID periodId,
        UUID employeeId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal amountSnapshot,
        PayrollPeriodStatus status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId,
        BigDecimal transactionAmount,
        String transactionCategory
) {
}
