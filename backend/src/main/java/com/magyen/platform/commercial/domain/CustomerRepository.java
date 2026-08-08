package com.magyen.platform.commercial.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link Customer}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);
}
