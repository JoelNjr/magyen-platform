package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionCostsByCommercialOrderQuery;
import com.magyen.platform.production.application.dto.GetProductionCostsByCommercialOrderResult;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.ProductionLaborCostSummary;
import com.magyen.platform.production.application.dto.ProductionMaterialCostSummary;
import com.magyen.platform.production.application.port.ProductionMaterialCostInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialHistoricalCost;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Consulta los costos de material (histórico Inventory) y mano de obra
 * atribuibles a una Orden comercial mediante su Orden de Producción.
 * <p>
 * Reutiliza la misma atribución de costo histórico que
 * {@link GetProductionOrderUseCase} / {@link GetProductionMaterialConsumptionsUseCase}.
 * No escribe en Finance ni modifica Inventory.
 */
public class GetProductionCostsByCommercialOrderUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort;

    public GetProductionCostsByCommercialOrderUseCase(
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

    public GetProductionCostsByCommercialOrderResult execute(GetProductionCostsByCommercialOrderQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.orderId(), "Order id must not be null");

        Optional<ProductionOrder> productionOrder = productionOrderRepository.findByOrderId(query.orderId());
        if (productionOrder.isEmpty()) {
            return emptyResult();
        }

        return toResult(productionOrder.get());
    }

    private GetProductionCostsByCommercialOrderResult emptyResult() {
        return new GetProductionCostsByCommercialOrderResult(
                null,
                false,
                ZERO_MONEY,
                0,
                0,
                0,
                ZERO_MONEY,
                0,
                0,
                0
        );
    }

    private GetProductionCostsByCommercialOrderResult toResult(ProductionOrder productionOrder) {
        List<GetProductionMaterialConsumptionResult> enrichedConsumptions =
                productionOrder.getMaterialConsumptions().stream()
                        .map(this::toConsumptionCostResult)
                        .toList();

        ProductionMaterialCostSummary materialCostSummary =
                ProductionMaterialCostSummaryCalculator.from(enrichedConsumptions);
        ProductionLaborCostSummary laborCostSummary =
                ProductionLaborCostSummaryCalculator.from(productionOrder.getLaborWorks());

        BigDecimal materialCost = materialCostSummary.totalMaterialCost() == null
                ? ZERO_MONEY
                : materialCostSummary.totalMaterialCost();
        BigDecimal laborCost = laborCostSummary.totalLaborCost() == null
                ? ZERO_MONEY
                : laborCostSummary.totalLaborCost();

        return new GetProductionCostsByCommercialOrderResult(
                productionOrder.getId(),
                true,
                materialCost,
                materialCostSummary.consumptionCount(),
                materialCostSummary.valuedConsumptionCount(),
                materialCostSummary.unvaluedConsumptionCount(),
                laborCost,
                laborCostSummary.laborWorkCount(),
                laborCostSummary.pendingCount(),
                laborCostSummary.paidCount()
        );
    }

    private GetProductionMaterialConsumptionResult toConsumptionCostResult(
            ProductionMaterialConsumption consumption
    ) {
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
