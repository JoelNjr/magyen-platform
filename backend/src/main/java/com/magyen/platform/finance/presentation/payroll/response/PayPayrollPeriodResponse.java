package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP del pago de un período de nómina.
 */
public record PayPayrollPeriodResponse(
        UUID periodId,
        UUID employeeId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal amountSnapshot,
        String status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId,
        BigDecimal transactionAmount,
        String transactionCategory
) {
}
