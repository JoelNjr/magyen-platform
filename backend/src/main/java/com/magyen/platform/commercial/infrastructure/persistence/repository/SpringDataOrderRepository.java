package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link OrderEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, UUID> {
}
