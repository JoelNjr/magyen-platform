package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para registrar trabajo de mano de obra en una Orden de Producción.
 */
public record RegisterProductionLaborWorkCommand(
        UUID productionOrderId,
        UUID operatorEmployeeId,
        LocalDate workDate,
        String operation,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitRate,
        String observation
) {
}
