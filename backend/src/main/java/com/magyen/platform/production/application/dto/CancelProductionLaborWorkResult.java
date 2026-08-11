package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionLaborWorkStatus;

import java.util.UUID;

/**
 * Resultado de la cancelación de mano de obra por producción.
 */
public record CancelProductionLaborWorkResult(
        UUID laborWorkId,
        UUID productionOrderId,
        ProductionLaborWorkStatus status
) {
}
