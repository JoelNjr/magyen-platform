package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionLaborWorkStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del pago de mano de obra por producción.
 */
public record PayProductionLaborWorkResult(
        UUID laborWorkId,
        UUID productionOrderId,
        ProductionLaborWorkStatus status,
        BigDecimal calculatedAmount,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
