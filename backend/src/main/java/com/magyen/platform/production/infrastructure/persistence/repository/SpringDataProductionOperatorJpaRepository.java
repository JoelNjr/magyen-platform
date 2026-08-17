package com.magyen.platform.production.infrastructure.persistence.repository;

import com.magyen.platform.production.infrastructure.persistence.entity.ProductionOperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link ProductionOperatorEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataProductionOperatorJpaRepository extends JpaRepository<ProductionOperatorEntity, UUID> {

    List<ProductionOperatorEntity> findByActiveTrue();
}
