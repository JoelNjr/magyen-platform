package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP del pago de mano de obra.
 */
public record PayProductionLaborWorkResponse(
        UUID laborWorkId,
        UUID productionOrderId,
        String status,
        BigDecimal calculatedAmount,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
