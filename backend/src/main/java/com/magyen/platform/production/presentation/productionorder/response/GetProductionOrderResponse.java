package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle completo de una Orden de Producción.
 */
public record GetProductionOrderResponse(
        UUID productionOrderId,
        UUID orderId,
        String orderNumber,
        String orderDescription,
        UUID customerId,
        String customerName,
        LocalDate creationDate,
        String status,
        String priority,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDate actualStartDate,
        LocalDate actualCompletionDate,
        String observations,
        List<ProductionItemResponse> items,
        List<ProductionOperationResponse> operations,
        ProductionMaterialCostSummaryResponse materialCostSummary,
        ProductionLaborCostSummaryResponse laborCostSummary,
        ProductionOtherCostSummaryResponse otherCostSummary,
        List<ProductionAdditionalCostResponse> additionalCosts,
        BigDecimal totalProductionCost,
        boolean hasReferenceImage
) {
}
