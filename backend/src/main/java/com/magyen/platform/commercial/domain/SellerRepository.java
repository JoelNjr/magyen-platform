package com.magyen.platform.commercial.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port leftover para resolver nombres históricos de vendedores.
 * <p>
 * No es la fuente de verdad. Los vendedores nuevos son {@code PayrollEmployee}.
 */
public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(UUID id);

    List<Seller> findAll();

    List<Seller> findAllActive();
}
