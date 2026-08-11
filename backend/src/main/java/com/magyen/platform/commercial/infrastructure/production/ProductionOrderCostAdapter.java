package com.magyen.platform.commercial.infrastructure.production;

import com.magyen.platform.commercial.application.port.ProductionOrderCostPort;
import com.magyen.platform.production.application.dto.GetProductionCostsByCommercialOrderQuery;
import com.magyen.platform.production.application.dto.GetProductionCostsByCommercialOrderResult;
import com.magyen.platform.production.application.usecase.GetProductionCostsByCommercialOrderUseCase;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador Commercial → Production para costos atribuibles a una Orden (solo lectura).
 */
public class ProductionOrderCostAdapter implements ProductionOrderCostPort {

    private final GetProductionCostsByCommercialOrderUseCase getProductionCostsByCommercialOrderUseCase;

    public ProductionOrderCostAdapter(
            GetProductionCostsByCommercialOrderUseCase getProductionCostsByCommercialOrderUseCase
    ) {
        this.getProductionCostsByCommercialOrderUseCase = Objects.requireNonNull(
                getProductionCostsByCommercialOrderUseCase,
                "Get production costs by commercial order use case must not be null"
        );
    }

    @Override
    public ProductionOrderCostSnapshot findCostsByOrderId(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        GetProductionCostsByCommercialOrderResult result =
                getProductionCostsByCommercialOrderUseCase.execute(
                        new GetProductionCostsByCommercialOrderQuery(orderId)
                );

        return new ProductionOrderCostSnapshot(
                result.productionOrderFound(),
                result.productionOrderId(),
                result.materialCost(),
                result.materialConsumptionCount(),
                result.valuedMaterialConsumptionCount(),
                result.unvaluedMaterialConsumptionCount(),
                result.laborCost(),
                result.laborWorkCount()
        );
    }
}
