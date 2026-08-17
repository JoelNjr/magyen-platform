package com.magyen.platform.commercial.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link Seller}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(UUID id);

    List<Seller> findAll();

    List<Seller> findAllActive();
}
