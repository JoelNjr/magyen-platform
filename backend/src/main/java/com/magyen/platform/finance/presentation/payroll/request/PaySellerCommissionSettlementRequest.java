package com.magyen.platform.finance.presentation.payroll.request;

import java.time.LocalDate;

/**
 * Pago HTTP de la comisión mensual de un vendedor.
 */
public record PaySellerCommissionSettlementRequest(
        LocalDate periodStart,
        LocalDate paymentDate,
        String observation
) {
}
