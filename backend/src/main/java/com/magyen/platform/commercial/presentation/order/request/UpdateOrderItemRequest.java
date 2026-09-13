package com.magyen.platform.commercial.presentation.order.request;

import java.math.BigDecimal;

public record UpdateOrderItemRequest(
        int quantity,
        BigDecimal unitPrice
) {
}
