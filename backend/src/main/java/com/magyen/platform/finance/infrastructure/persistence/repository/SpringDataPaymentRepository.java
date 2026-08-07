package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link PaymentEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataPaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findByOrderId(UUID orderId);
}
