package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.ProductionOtherCostSummary;
import com.magyen.platform.production.domain.ProductionAdditionalCost;

import java.math.BigDecimal;
import java.util.List;

final class ProductionOtherCostSummaryCalculator {

    private ProductionOtherCostSummaryCalculator() {
    }

    static ProductionOtherCostSummary from(List<ProductionAdditionalCost> additionalCosts) {
        if (additionalCosts == null || additionalCosts.isEmpty()) {
            return new ProductionOtherCostSummary(null, 0);
        }

        BigDecimal total = additionalCosts.stream()
                .map(cost -> cost.getAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductionOtherCostSummary(total, additionalCosts.size());
    }
}
