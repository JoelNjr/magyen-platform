package com.magyen.platform.production.infrastructure.persistence.repository;

import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link ProductionOrderEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataProductionOrderRepository extends JpaRepository<ProductionOrderEntity, UUID> {

    Optional<ProductionOrderEntity> findByOrderId(UUID orderId);
}
