package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionStatus;

import java.util.UUID;

/**
 * Resultado del caso de uso de inicio de una Orden de Producción.
 */
public record StartProductionOrderResult(
        UUID productionOrderId,
        ProductionStatus status
) {
}
