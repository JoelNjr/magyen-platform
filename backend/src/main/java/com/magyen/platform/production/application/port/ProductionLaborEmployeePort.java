package com.magyen.platform.production.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Application para resolver operarios de producción.
 * <p>
 * Los operarios son empleados de Finance {@code PayrollEmployee} con
 * compensación {@code PRODUCTION_BASED}. No hay un catálogo propio de Production.
 */
public interface ProductionLaborEmployeePort {

    ProductionLaborOperatorInfo requireEligibleProductionOperator(UUID operatorEmployeeId);

    List<ProductionLaborOperatorInfo> listActiveProductionBasedOperators();

    /**
     * Lectura suave de nombre para enriquecimiento de historial (sin validar elegibilidad).
     */
    Optional<String> findOperatorDisplayName(UUID operatorEmployeeId);
}
