package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionLaborWorkStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de lectura de un registro de mano de obra.
 */
public record GetProductionLaborWorkResult(
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
        ProductionLaborWorkStatus status,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
