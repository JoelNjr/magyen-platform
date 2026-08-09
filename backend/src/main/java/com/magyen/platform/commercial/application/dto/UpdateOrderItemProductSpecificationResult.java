package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Resultado del caso de uso que actualiza la especificación comercial de un OrderItem.
 */
public record UpdateOrderItemProductSpecificationResult(
        UUID orderItemId,
        ProductSpecificationResult productSpecification
) {
}
