package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Comparator;
import java.util.Objects;

/**
 * Caso de uso que consulta el historial de consumos de material de una Orden de Producción.
 * <p>
 * Devuelve los registros más recientes primero.
 */
public class GetProductionMaterialConsumptionsUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public GetProductionMaterialConsumptionsUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public GetProductionMaterialConsumptionsResult execute(GetProductionMaterialConsumptionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(query.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + query.productionOrderId()
                ));

        return new GetProductionMaterialConsumptionsResult(
                productionOrder.getMaterialConsumptions().stream()
                        .sorted(Comparator
                                .comparing(ProductionMaterialConsumption::getConsumptionDate)
                                .reversed()
                                .thenComparing(ProductionMaterialConsumption::getId, Comparator.reverseOrder()))
                        .map(GetProductionMaterialConsumptionsUseCase::toResult)
                        .toList()
        );
    }

    private static GetProductionMaterialConsumptionResult toResult(ProductionMaterialConsumption consumption) {
        return new GetProductionMaterialConsumptionResult(
                consumption.getId(),
                consumption.getProductionOrderId(),
                consumption.getInventoryItemId(),
                consumption.getQuantity(),
                consumption.getUnitOfMeasure().name(),
                consumption.getConsumptionDate(),
                consumption.getObservation()
        );
    }
}
