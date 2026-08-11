package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsQuery;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionsResult;
import com.magyen.platform.production.application.port.ProductionMaterialCostInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialHistoricalCost;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso que consulta el historial de consumos de material de una Orden de Producción
 * y atribuye el costo histórico desde Inventory.
 * <p>
 * No crea ni consulta transacciones financieras: el consumo es atribución de costo,
 * no un gasto de caja.
 */
public class GetProductionMaterialConsumptionsUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort;

    public GetProductionMaterialConsumptionsUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionMaterialCostInventoryPort = Objects.requireNonNull(
                productionMaterialCostInventoryPort,
                "Production material cost inventory port must not be null"
        );
    }

    public GetProductionMaterialConsumptionsResult execute(GetProductionMaterialConsumptionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(query.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + query.productionOrderId()
                ));

        List<GetProductionMaterialConsumptionResult> consumptions = productionOrder.getMaterialConsumptions().stream()
                .sorted(Comparator
                        .comparing(ProductionMaterialConsumption::getConsumptionDate)
                        .reversed()
                        .thenComparing(ProductionMaterialConsumption::getId, Comparator.reverseOrder()))
                .map(this::toResult)
                .toList();

        return new GetProductionMaterialConsumptionsResult(
                consumptions,
                ProductionMaterialCostSummaryCalculator.from(consumptions)
        );
    }

    private GetProductionMaterialConsumptionResult toResult(ProductionMaterialConsumption consumption) {
        Optional<ProductionMaterialHistoricalCost> historicalCost =
                productionMaterialCostInventoryPort.findHistoricalCost(consumption.getId());

        return new GetProductionMaterialConsumptionResult(
                consumption.getId(),
                consumption.getProductionOrderId(),
                consumption.getInventoryItemId(),
                consumption.getQuantity(),
                consumption.getUnitOfMeasure().name(),
                consumption.getConsumptionDate(),
                consumption.getObservation(),
                historicalCost.map(ProductionMaterialHistoricalCost::unitCost).orElse(null),
                historicalCost.map(ProductionMaterialHistoricalCost::totalCost).orElse(null)
        );
    }
}
