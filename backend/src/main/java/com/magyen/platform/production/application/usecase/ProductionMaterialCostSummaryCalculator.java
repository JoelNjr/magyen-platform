package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.ProductionMaterialCostSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Calcula el resumen de atribución de costo a partir de consumos ya enriquecidos
 * con el snapshot histórico de Inventory.
 */
final class ProductionMaterialCostSummaryCalculator {

    private ProductionMaterialCostSummaryCalculator() {
    }

    static ProductionMaterialCostSummary from(List<GetProductionMaterialConsumptionResult> consumptions) {
        Objects.requireNonNull(consumptions, "Consumptions must not be null");

        int valuedConsumptionCount = 0;
        int unvaluedConsumptionCount = 0;
        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        boolean hasValuedConsumption = false;

        for (GetProductionMaterialConsumptionResult consumption : consumptions) {
            if (consumption.totalCost() != null) {
                valuedConsumptionCount++;
                hasValuedConsumption = true;
                totalMaterialCost = totalMaterialCost.add(consumption.totalCost());
            } else {
                unvaluedConsumptionCount++;
            }
        }

        return new ProductionMaterialCostSummary(
                hasValuedConsumption ? totalMaterialCost : null,
                consumptions.size(),
                valuedConsumptionCount,
                unvaluedConsumptionCount
        );
    }
}
