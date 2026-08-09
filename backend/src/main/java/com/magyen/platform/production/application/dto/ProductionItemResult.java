package com.magyen.platform.production.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Representación de un ítem del snapshot productivo para casos de uso de consulta.
 */
public record ProductionItemResult(
        UUID productionItemId,
        String productName,
        int quantity,
        ProductionProductSpecificationResult productSpecification,
        List<ProductionSizeBreakdownResult> sizes
) {
}
