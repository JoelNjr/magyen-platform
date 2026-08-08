package com.magyen.platform.commercial.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link Quotation}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface QuotationRepository {

    Quotation save(Quotation quotation);

    Optional<Quotation> findById(UUID id);

    List<Quotation> findAll();
}
