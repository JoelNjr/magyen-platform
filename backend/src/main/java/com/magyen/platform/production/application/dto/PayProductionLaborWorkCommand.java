package com.magyen.platform.production.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para pagar un registro de mano de obra PENDING.
 */
public record PayProductionLaborWorkCommand(
        UUID productionOrderId,
        UUID laborWorkId,
        LocalDate paymentDate,
        String observation
) {
}
