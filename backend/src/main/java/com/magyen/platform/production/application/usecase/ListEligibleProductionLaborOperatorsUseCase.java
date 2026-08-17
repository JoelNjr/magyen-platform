package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.port.ProductionLaborEmployeePort;
import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;

import java.util.List;
import java.util.Objects;

/**
 * Lista operarios de producción activos para el selector de mano de obra.
 */
public class ListEligibleProductionLaborOperatorsUseCase {

    private final ProductionLaborEmployeePort productionLaborEmployeePort;

    public ListEligibleProductionLaborOperatorsUseCase(
            ProductionLaborEmployeePort productionLaborEmployeePort
    ) {
        this.productionLaborEmployeePort = Objects.requireNonNull(
                productionLaborEmployeePort,
                "Production labor employee port must not be null"
        );
    }

    public List<ProductionLaborOperatorInfo> execute() {
        return productionLaborEmployeePort.listActiveProductionBasedOperators();
    }
}
