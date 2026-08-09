package com.magyen.platform.commercial.presentation.order.response;

import com.magyen.platform.commercial.presentation.quotation.response.ProductSpecificationResponse;

import java.util.UUID;

/**
 * Respuesta HTTP tras actualizar la especificación comercial de un OrderItem.
 */
public record UpdateOrderItemProductSpecificationResponse(
        UUID orderItemId,
        ProductSpecificationResponse productSpecification
) {
}
