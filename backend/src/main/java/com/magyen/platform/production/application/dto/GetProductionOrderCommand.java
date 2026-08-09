package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso que consulta una Orden de Producción por identificador.
 */
public record GetProductionOrderCommand(
        UUID productionOrderId
) {
}
