package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterProductionAdditionalCostCommand(
        UUID productionOrderId,
        String category,
        String description,
        BigDecimal amount,
        LocalDate incurredDate
) {
}
