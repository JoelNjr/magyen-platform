package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.intelligence.application.dto.GetProductionReportResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso que consolida el reporte de producción agrupando Órdenes de Producción por estado.
 * <p>
 * Solo consulta información existente; no modifica el estado del negocio.
 */
public class GetProductionReportUseCase {

    private final ProductionOrderRepository productionOrderRepository;

    public GetProductionReportUseCase(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
    }

    public GetProductionReportResult execute() {
        List<ProductionOrder> productionOrders = productionOrderRepository.findAll();

        Map<ProductionStatus, Long> countByStatus = new EnumMap<>(ProductionStatus.class);
        for (ProductionStatus status : ProductionStatus.values()) {
            countByStatus.put(status, 0L);
        }

        for (ProductionOrder productionOrder : productionOrders) {
            countByStatus.merge(productionOrder.getStatus(), 1L, Long::sum);
        }

        return new GetProductionReportResult(Map.copyOf(countByStatus));
    }
}
