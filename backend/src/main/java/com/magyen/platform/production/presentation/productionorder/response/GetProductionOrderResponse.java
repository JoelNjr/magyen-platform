package com.magyen.platform.production.presentation.productionorder.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle completo de una Orden de Producción.
 */
public record GetProductionOrderResponse(
        UUID productionOrderId,
        UUID orderId,
        LocalDate creationDate,
        String status,
        String priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String observations,
        List<ProductionItemResponse> items,
        List<ProductionOperationResponse> operations
) {
}
