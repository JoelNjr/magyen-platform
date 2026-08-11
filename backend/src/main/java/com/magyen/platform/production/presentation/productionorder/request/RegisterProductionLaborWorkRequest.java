package com.magyen.platform.production.presentation.productionorder.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para registrar mano de obra en producción.
 */
public record RegisterProductionLaborWorkRequest(
        UUID operatorEmployeeId,
        LocalDate workDate,
        String operation,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitRate,
        String observation
) {
}
