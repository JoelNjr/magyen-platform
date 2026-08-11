package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionLaborWorkStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del registro de mano de obra por producción.
 */
public record RegisterProductionLaborWorkResult(
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
        ProductionLaborWorkStatus status
) {
}
