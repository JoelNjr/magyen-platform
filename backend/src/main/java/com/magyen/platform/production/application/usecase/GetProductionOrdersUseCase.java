package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta las Órdenes de Producción existentes.
 */
public class GetProductionOrdersUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public GetProductionOrdersUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public GetProductionOrdersResult execute() {
        List<ProductionOrderResult> productionOrders = productionOrderRepository.findAll().stream()
                .map(this::toProductionOrderResult)
                .toList();

        return new GetProductionOrdersResult(productionOrders);
    }

    private ProductionOrderResult toProductionOrderResult(ProductionOrder productionOrder) {
        return new ProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderId(),
                productionOrder.getCreationDate(),
                productionOrder.getStatus(),
                productionOrder.getPriority(),
                productionOrder.getPlannedStartDate(),
                productionOrder.getPlannedEndDate(),
                productionOrder.getObservations()
        );
    }
}
