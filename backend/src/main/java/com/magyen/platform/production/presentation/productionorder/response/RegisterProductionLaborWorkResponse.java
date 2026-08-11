package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP del registro de mano de obra.
 */
public record RegisterProductionLaborWorkResponse(
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
        String status
) {
}
