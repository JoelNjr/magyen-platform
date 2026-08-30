package com.magyen.platform.production.presentation.productionorder.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterProductionAdditionalCostRequest(
        String category,
        String description,
        BigDecimal amount,
        LocalDate incurredDate
) {
}
