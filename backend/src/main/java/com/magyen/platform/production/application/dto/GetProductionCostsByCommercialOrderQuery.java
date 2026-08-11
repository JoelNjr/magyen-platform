package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Consulta de costos de producción atribuibles a una Orden comercial.
 */
public record GetProductionCostsByCommercialOrderQuery(
        UUID orderId
) {
}
