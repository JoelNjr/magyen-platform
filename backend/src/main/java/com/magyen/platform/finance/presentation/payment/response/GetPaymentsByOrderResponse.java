package com.magyen.platform.finance.presentation.payment.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta HTTP con los pagos de una Orden, el total pagado y el saldo restante.
 */
public record GetPaymentsByOrderResponse(
        List<GetPaymentResponse> payments,
        BigDecimal totalPaid,
        BigDecimal remainingBalance
) {
}
