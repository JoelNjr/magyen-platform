package com.magyen.platform.production.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Application para resolver operarios de producción.
 * <p>
 * La implementación V1 usa {@code ProductionOperator}, no nómina ni usuarios de login.
 */
public interface ProductionLaborEmployeePort {

    ProductionLaborOperatorInfo requireEligibleProductionOperator(UUID operatorEmployeeId);

    List<ProductionLaborOperatorInfo> listActiveProductionBasedOperators();

    /**
     * Lectura suave de nombre para enriquecimiento de historial (sin validar elegibilidad).
     */
    Optional<String> findOperatorDisplayName(UUID operatorEmployeeId);
}
