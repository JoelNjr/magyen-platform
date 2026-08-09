package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.application.dto.ProductionOperationResult;
import com.magyen.platform.production.application.dto.ProductionProductSpecificationResult;
import com.magyen.platform.production.application.dto.ProductionSizeBreakdownResult;
import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOperation;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.SizeBreakdown;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta una Orden de Producción completa por identificador.
 */
public class GetProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public GetProductionOrderUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
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

        return new GetProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getObservations(),
                items,
                operations
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
