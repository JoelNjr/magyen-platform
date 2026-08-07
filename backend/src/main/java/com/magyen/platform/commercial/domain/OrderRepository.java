package com.magyen.platform.commercial.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link Order}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findAll();
}
