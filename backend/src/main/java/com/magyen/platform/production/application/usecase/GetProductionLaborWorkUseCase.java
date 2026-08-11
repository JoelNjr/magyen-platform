package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionLaborWorkQuery;
import com.magyen.platform.production.application.dto.GetProductionLaborWorkResult;
import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.domain.ProductionLaborWork;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;

import java.util.Objects;

/**
 * Consulta un registro de mano de obra de una Orden de Producción.
 */
public class GetProductionLaborWorkUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionLaborEmployeePort productionLaborEmployeePort;

    public GetProductionLaborWorkUseCase(
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

    public GetProductionLaborWorkResult execute(GetProductionLaborWorkQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.productionOrderId(), "Production order id must not be null");
        Objects.requireNonNull(query.laborWorkId(), "Labor work id must not be null");

        ProductionOrder productionOrder = productionOrderRepository.findById(query.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Production order not found: " + query.productionOrderId()
                ));

        ProductionLaborWork laborWork = productionOrder.requireLaborWork(query.laborWorkId());
        String operatorDisplayName = productionLaborEmployeePort
                .findOperatorDisplayName(laborWork.getOperatorEmployeeId())
                .orElse(null);

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
