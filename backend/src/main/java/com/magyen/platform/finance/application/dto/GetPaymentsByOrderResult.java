package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del caso de uso de consulta de pagos por Orden,
 * incluyendo el total pagado y el saldo restante.
 */
public record GetPaymentsByOrderResult(
        List<GetPaymentResult> payments,
        BigDecimal totalPaid,
        BigDecimal remainingBalance
) {
}
