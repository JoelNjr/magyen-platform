package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductionAdditionalCostResponse(
        UUID additionalCostId,
        UUID productionOrderId,
        String category,
        String description,
        BigDecimal amount,
        LocalDate incurredDate,
        UUID financialTransactionId
) {
}
