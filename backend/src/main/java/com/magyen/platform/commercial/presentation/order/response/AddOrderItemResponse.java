package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AddOrderItemResponse(
        UUID orderId,
        UUID itemId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
