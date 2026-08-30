package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyQuotationDiscountCommand(
        UUID quotationId,
        BigDecimal discountAmount
) {
}
