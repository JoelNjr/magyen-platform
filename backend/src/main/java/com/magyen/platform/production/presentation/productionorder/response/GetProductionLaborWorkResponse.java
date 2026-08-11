package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP de un registro de mano de obra.
 */
public record GetProductionLaborWorkResponse(
        UUID laborWorkId,
        UUID productionOrderId,
        UUID operatorEmployeeId,
        String operatorDisplayName,
        LocalDate workDate,
        String operation,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitRate,
        BigDecimal calculatedAmount,
        String observation,
        String status,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
