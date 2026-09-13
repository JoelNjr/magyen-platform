package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada para agregar un producto comercial a una Orden existente.
 */
public record AddOrderItemCommand(
        UUID orderId,
        String productName,
        int quantity,
        String fabric,
        String secondaryFabric,
        String color,
        BigDecimal unitPrice,
        ProductSpecificationCommand productSpecification
) {
}
