package com.magyen.platform.production.application.dto;

import java.util.List;

/**
 * Resultado del caso de uso que consulta las Órdenes de Producción existentes.
 */
public record GetProductionOrdersResult(
        List<ProductionOrderResult> productionOrders
) {
}
