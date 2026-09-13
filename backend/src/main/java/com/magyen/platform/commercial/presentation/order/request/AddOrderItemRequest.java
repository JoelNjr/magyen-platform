package com.magyen.platform.commercial.presentation.order.request;

import com.magyen.platform.commercial.presentation.quotation.request.ProductSpecificationRequest;

import java.math.BigDecimal;

public record AddOrderItemRequest(
        String productName,
        int quantity,
        String fabric,
        String secondaryFabric,
        String color,
        BigDecimal unitPrice,
        ProductSpecificationRequest productSpecification
) {
}
