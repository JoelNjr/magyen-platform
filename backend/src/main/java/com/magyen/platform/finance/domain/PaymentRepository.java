package com.magyen.platform.finance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link Payment}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findByOrderId(UUID orderId);
}
