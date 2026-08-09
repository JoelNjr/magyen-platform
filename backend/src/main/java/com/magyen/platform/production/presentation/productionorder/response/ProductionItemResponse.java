package com.magyen.platform.production.presentation.productionorder.response;

import java.util.List;
import java.util.UUID;

/**
 * Ítem del snapshot productivo expuesto por la API de consulta.
 */
public record ProductionItemResponse(
        UUID productionItemId,
        String productName,
        int quantity,
        ProductionProductSpecificationResponse productSpecification,
        List<ProductionSizeBreakdownResponse> sizes
) {
}
