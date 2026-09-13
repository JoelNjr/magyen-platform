package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateOrderItemResponse(
        UUID orderId,
        UUID itemId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal itemSubtotal,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
