package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link OrderEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Usa findFirst porque el entorno de desarrollo puede contener duplicados históricos
     * hasta aplicar UNIQUE(quotation_id).
     */
    Optional<OrderEntity> findFirstByQuotationId(UUID quotationId);
}
