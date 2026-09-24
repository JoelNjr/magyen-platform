package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Línea de comisión de un pedido en el mes de confirmación.
 */
public record PayrollEmployeeCommissionOrderResponse(
        UUID orderId,
        String orderNumber,
        String customerName,
        LocalDate confirmationDate,
        BigDecimal orderTotal,
        BigDecimal commissionRate,
        BigDecimal commissionAmount
) {
}
