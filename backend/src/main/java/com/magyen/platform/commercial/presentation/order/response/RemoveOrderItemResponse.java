package com.magyen.platform.commercial.presentation.order.response;

import java.math.BigDecimal;
import java.util.UUID;

public record RemoveOrderItemResponse(
        UUID orderId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
