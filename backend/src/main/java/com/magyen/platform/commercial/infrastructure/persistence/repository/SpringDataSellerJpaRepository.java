package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.infrastructure.persistence.entity.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link SellerEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataSellerJpaRepository extends JpaRepository<SellerEntity, UUID> {

    List<SellerEntity> findByActiveTrue();
}
