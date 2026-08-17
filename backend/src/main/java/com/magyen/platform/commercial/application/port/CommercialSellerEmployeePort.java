package com.magyen.platform.commercial.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Application para resolver vendedores comerciales.
 * <p>
 * Los vendedores son empleados de Finance {@code PayrollEmployee} con
 * compensación {@code FIXED_PAYROLL}. Commercial no mantiene un catálogo propio.
 */
public interface CommercialSellerEmployeePort {

    CommercialSellerEmployeeInfo requireEligibleSeller(UUID sellerEmployeeId);

    List<CommercialSellerEmployeeInfo> listActiveFixedSellers();

    /**
     * Lectura suave de nombre para enriquecimiento histórico (sin validar elegibilidad).
     */
    Optional<String> findEmployeeDisplayName(UUID sellerEmployeeId);

    Map<UUID, String> findEmployeeDisplayNames(Collection<UUID> sellerEmployeeIds);
}
