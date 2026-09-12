package com.magyen.platform.commercial.domain;

import java.time.LocalDate;
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

    Optional<Order> findByQuotationId(UUID quotationId);

    List<Order> findAll();

    /**
     * Órdenes cuya entrega programada cae en {@code [fromDate, toDate]} (inclusive).
     * <p>
     * El filtro se aplica en persistencia. No sustituye el listado por {@code confirmationDate}.
     */
    List<Order> findByPromisedDeliveryDateBetween(LocalDate fromDate, LocalDate toDate);
}
