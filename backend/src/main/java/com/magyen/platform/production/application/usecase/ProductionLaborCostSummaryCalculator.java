package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.ProductionLaborCostSummary;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionLaborWorkStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Calcula el resumen de costo de mano de obra (PENDING + PAID; CANCELLED excluido).
 */
final class ProductionLaborCostSummaryCalculator {

    private ProductionLaborCostSummaryCalculator() {
    }

    static ProductionLaborCostSummary from(List<ProductionLaborWork> laborWorks) {
        Objects.requireNonNull(laborWorks, "Labor works must not be null");

        int pendingCount = 0;
        int paidCount = 0;
        BigDecimal totalLaborCost = BigDecimal.ZERO;

        for (ProductionLaborWork laborWork : laborWorks) {
            if (laborWork.getStatus() == ProductionLaborWorkStatus.CANCELLED) {
                continue;
            }
            if (laborWork.getStatus() == ProductionLaborWorkStatus.PENDING) {
                pendingCount++;
            } else if (laborWork.getStatus() == ProductionLaborWorkStatus.PAID) {
                paidCount++;
            }
            totalLaborCost = totalLaborCost.add(laborWork.getCalculatedAmount());
        }

        int laborWorkCount = pendingCount + paidCount;
        return new ProductionLaborCostSummary(
                laborWorkCount == 0 ? null : totalLaborCost,
                laborWorkCount,
                pendingCount,
                paidCount
        );
    }
}
