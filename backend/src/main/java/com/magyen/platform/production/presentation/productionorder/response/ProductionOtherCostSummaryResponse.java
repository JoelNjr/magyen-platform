package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;

public record ProductionOtherCostSummaryResponse(
        BigDecimal totalOtherCost,
        int otherCostCount
) {
}
