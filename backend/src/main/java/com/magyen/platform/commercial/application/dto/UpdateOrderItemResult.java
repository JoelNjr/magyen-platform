package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateOrderItemResult(
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
