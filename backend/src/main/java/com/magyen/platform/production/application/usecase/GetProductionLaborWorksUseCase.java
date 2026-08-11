package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorksResult;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lista registros de mano de obra de una Orden de Producción (más recientes primero).
 */
public class GetProductionLaborWorksUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionLaborEmployeePort productionLaborEmployeePort;

    public GetProductionLaborWorksUseCase(
            ProductionOrderRepository productionOrderRepository,
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        this.productionOrderRepository = Objects.requireNonNull(
                productionOrderRepository,
                "Production order repository must not be null"
        );
        this.productionLaborEmployeePort = Objects.requireNonNull(
                productionLaborEmployeePort,
                "Production labor employee port must not be null"
        );
    }

    public GetProductionLaborWorksResult execute(GetProductionLaborWorksQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.productionOrderId(), "Production order id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(query.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + query.productionOrderId()
                ));

        Map<UUID, String> displayNamesByEmployeeId = productionLaborEmployeePort
                .listActiveProductionBasedOperators()
                .stream()
                .collect(Collectors.toMap(
                        ProductionLaborOperatorInfo::employeeId,
                        ProductionLaborOperatorInfo::displayName,
                        (left, right) -> left
                ));

        List<GetProductionLaborWorkResult> laborWorks = productionOrder.getLaborWorks().stream()
                .sorted(Comparator
                        .comparing(ProductionLaborWork::getWorkDate)
                        .thenComparing(ProductionLaborWork::getId)
                        .reversed())
                .map(laborWork -> toResult(laborWork, resolveDisplayName(laborWork, displayNamesByEmployeeId)))
                .toList();

        return new GetProductionLaborWorksResult(
                laborWorks,
                ProductionLaborCostSummaryCalculator.from(productionOrder.getLaborWorks())
        );
    }

    private String resolveDisplayName(
            ProductionLaborWork laborWork,
            Map<UUID, String> displayNamesByEmployeeId
    ) {
        String cached = displayNamesByEmployeeId.get(laborWork.getOperatorEmployeeId());
        if (cached != null) {
            return cached;
        }
        return productionLaborEmployeePort
                .findOperatorDisplayName(laborWork.getOperatorEmployeeId())
                .orElse(null);
    }

    private GetProductionLaborWorkResult toResult(ProductionLaborWork laborWork, String operatorDisplayName) {
        return new GetProductionLaborWorkResult(
                laborWork.getId(),
                laborWork.getProductionOrderId(),
                laborWork.getOperatorEmployeeId(),
                operatorDisplayName,
                laborWork.getWorkDate(),
                laborWork.getOperation(),
                laborWork.getQuantity(),
                laborWork.getUnitOfMeasure(),
                laborWork.getUnitRate(),
                laborWork.getCalculatedAmount(),
                laborWork.getObservation(),
                laborWork.getStatus(),
                laborWork.getPaidAt(),
                laborWork.getFinancialTransactionId()
        );
    }
}
