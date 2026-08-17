package com.magyen.platform.production.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link ProductionOperator}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface ProductionOperatorRepository {

    ProductionOperator save(ProductionOperator productionOperator);

    Optional<ProductionOperator> findById(UUID id);

    List<ProductionOperator> findAll();

    List<ProductionOperator> findAllActive();
}
