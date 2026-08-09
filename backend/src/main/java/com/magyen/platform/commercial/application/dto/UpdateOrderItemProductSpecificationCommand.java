package com.magyen.platform.commercial.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para actualizar la especificación comercial de un OrderItem.
 */
public record UpdateOrderItemProductSpecificationCommand(
        UUID orderId,
        UUID orderItemId,
        ProductSpecificationCommand productSpecification
) {
}
