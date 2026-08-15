package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.CommercialOrderIdentityResolver;
import com.magyen.platform.production.application.CommercialOrderIdentityResolver.CommercialOrderIdentity;
import com.magyen.platform.production.application.dto.GetProductionMaterialConsumptionResult;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.application.dto.ProductionLaborCostSummary;
import com.magyen.platform.production.application.dto.ProductionMaterialCostSummary;
import com.magyen.platform.production.application.dto.ProductionOperationResult;
import com.magyen.platform.production.application.dto.ProductionProductSpecificationResult;
import com.magyen.platform.production.application.dto.ProductionSizeBreakdownResult;
import com.magyen.platform.production.application.port.ProductionMaterialCostInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialHistoricalCost;
import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionMaterialConsumption;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.SizeBreakdown;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Caso de uso que consulta una Orden de Producción completa por identificador,
 * incluyendo el resumen de atribución de costo histórico de materiales.
 */
public class GetProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort;
    private final CommercialOrderIdentityResolver commercialOrderIdentityResolver;

    public GetProductionOrderUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionMaterialCostInventoryPort productionMaterialCostInventoryPort,
            CommercialOrderIdentityResolver commercialOrderIdentityResolver
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionMaterialCostInventoryPort = Objects.requireNonNull(
                productionMaterialCostInventoryPort,
                "Production material cost inventory port must not be null"
        );
        this.commercialOrderIdentityResolver = Objects.requireNonNull(
                commercialOrderIdentityResolver,
                "Commercial order identity resolver must not be null"
        );
    }

    public GetProductionOrderResult execute(GetProductionOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + command.productionOrderId()
                ));

        return toResult(productionOrder);
    }

    private void validateCommand(GetProductionOrderCommand command) {
        Objects.requireNonNull(command.productionOrderId(), "Production order id must not be null");
    }

    private GetProductionOrderResult toResult(ProductionOrder productionOrder) {
        List<ProductionItemResult> items = productionOrder.getItems().stream()
                .map(this::toItemResult)
                .toList();

        List<ProductionOperationResult> operations = productionOrder.getOperations().stream()
                .map(this::toOperationResult)
                .toList();

        ProductionMaterialCostSummary materialCostSummary = ProductionMaterialCostSummaryCalculator.from(
                productionOrder.getMaterialConsumptions().stream()
                        .map(this::toConsumptionCostResult)
                        .toList()
        );

        ProductionLaborCostSummary laborCostSummary = ProductionLaborCostSummaryCalculator.from(
                productionOrder.getLaborWorks()
        );

        CommercialOrderIdentity commercialIdentity =
                commercialOrderIdentityResolver.resolve(productionOrder.getOrderId());

        return new GetProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                commercialIdentity.orderNumber(),
                commercialIdentity.customerId(),
                commercialIdentity.customerName(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getObservations(),
                items,
                operations,
                materialCostSummary,
                laborCostSummary,
                resolveTotalProductionCost(materialCostSummary, laborCostSummary)
        );
    }

    private BigDecimal resolveTotalProductionCost(
            ProductionMaterialCostSummary materialCostSummary,
            ProductionLaborCostSummary laborCostSummary
    ) {
        int materialCount = materialCostSummary == null ? 0 : materialCostSummary.consumptionCount();
        int laborCount = laborCostSummary == null ? 0 : laborCostSummary.laborWorkCount();
        if (materialCount + laborCount == 0) {
            return null;
        }

        BigDecimal material = materialCostSummary == null || materialCostSummary.totalMaterialCost() == null
                ? BigDecimal.ZERO
                : materialCostSummary.totalMaterialCost();
        BigDecimal labor = laborCostSummary == null || laborCostSummary.totalLaborCost() == null
                ? BigDecimal.ZERO
                : laborCostSummary.totalLaborCost();
        return material.add(labor);
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

    private ProductionItemResult toItemResult(ProductionItem item) {
        return new ProductionItemResult(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                toProductSpecificationResult(item.getProductSpecification()),
                item.getSizeBreakdowns().stream()
                        .map(this::toSizeBreakdownResult)
                        .toList()
        );
    }

    private ProductionProductSpecificationResult toProductSpecificationResult(
            ProductSpecification specification
    ) {
        ProductSpecification resolved = specification == null
                ? ProductSpecification.empty()
                : specification;

        return new ProductionProductSpecificationResult(
                resolved.getGarmentType(),
                resolved.getCollarType(),
                resolved.getSleeveType(),
                resolved.getGarmentVariant(),
                resolved.isSublimationRequired(),
                resolved.isEmbroideryRequired(),
                resolved.isDtfRequired(),
                resolved.getDecorationNotes(),
                resolved.isIncludesNames(),
                resolved.isIncludesNumbers(),
                resolved.isIncludesLogos(),
                resolved.getPersonalizationNotes(),
                resolved.getItemObservations()
        );
    }

    private ProductionSizeBreakdownResult toSizeBreakdownResult(SizeBreakdown sizeBreakdown) {
        return new ProductionSizeBreakdownResult(
                sizeBreakdown.getSize(),
                sizeBreakdown.getQuantity()
        );
    }

    private ProductionOperationResult toOperationResult(ProductionOperation operation) {
        return new ProductionOperationResult(
                operation.getId(),
                operation.getType(),
                operation.getStatus(),
                operation.getAssignedOperator(),
                operation.getPlannedStartDate(),
                operation.getPlannedEndDate(),
                operation.getActualStartDate(),
                operation.getActualEndDate(),
                operation.getObservations()
        );
    }
}
