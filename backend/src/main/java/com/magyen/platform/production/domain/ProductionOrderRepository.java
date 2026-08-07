package com.magyen.platform.production.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link ProductionOrder}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface ProductionOrderRepository {

    ProductionOrder save(ProductionOrder productionOrder);

    Optional<ProductionOrder> findById(UUID id);

    Optional<ProductionOrder> findByOrderId(UUID orderId);

    List<ProductionOrder> findAll();
}
