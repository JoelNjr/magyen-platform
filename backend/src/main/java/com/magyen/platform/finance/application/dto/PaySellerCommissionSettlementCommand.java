package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pago de la comisión de un vendedor en un mes de confirmación.
 */
public record PaySellerCommissionSettlementCommand(
        UUID employeeId,
        LocalDate periodStart,
        LocalDate paymentDate,
        String observation
) {
}
