package com.magyen.platform.commercial.presentation.order.request;

import java.math.BigDecimal;

public record ApplyOrderDiscountRequest(
        BigDecimal discountAmount
) {
}
