package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionStatus;

import java.util.UUID;

/**
 * Resultado del caso de uso de finalización de una Orden de Producción.
 */
public record CompleteProductionOrderResult(
        UUID productionOrderId,
        ProductionStatus status
) {
}
