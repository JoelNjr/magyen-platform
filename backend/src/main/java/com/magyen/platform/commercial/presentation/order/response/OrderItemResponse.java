package com.magyen.platform.commercial.presentation.order.response;

import com.magyen.platform.commercial.presentation.quotation.response.ProductSpecificationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Producto comprometido de una Orden expuesto por la API de detalle.
 */
public record OrderItemResponse(
        UUID itemId,
        String productName,
        int quantity,
        String fabric,
        String secondaryFabric,
        String color,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ProductSpecificationResponse productSpecification,
        List<SizeBreakdownResponse> sizes
) {
}
