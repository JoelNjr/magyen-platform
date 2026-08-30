package com.magyen.platform.production.application.dto;

import com.magyen.platform.production.domain.ProductionDirectCostCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterProductionAdditionalCostResult(
        UUID additionalCostId,
        UUID productionOrderId,
        ProductionDirectCostCategory category,
        String description,
        BigDecimal amount,
        LocalDate incurredDate,
        UUID financialTransactionId
) {
}
