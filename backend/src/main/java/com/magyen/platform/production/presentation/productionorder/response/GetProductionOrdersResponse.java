package com.magyen.platform.production.presentation.productionorder.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con las Órdenes de Producción existentes.
 */
public record GetProductionOrdersResponse(
        List<ProductionOrderResponse> productionOrders
) {

    /**
     * Orden de Producción expuesta por la API de consulta de listado.
     */
    public record ProductionOrderResponse(
            UUID productionOrderId,
            UUID orderId,
            LocalDate creationDate,
            String status,
            String priority,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            String observations
    ) {
    }
}
