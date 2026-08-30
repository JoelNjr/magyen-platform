package com.magyen.platform.commercial.presentation.quotation.request;

import java.math.BigDecimal;

public record ApplyQuotationDiscountRequest(
        BigDecimal discountAmount
) {
}
