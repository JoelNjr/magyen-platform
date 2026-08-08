package com.magyen.platform.commercial.infrastructure.persistence.repository;

import com.magyen.platform.commercial.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link CustomerEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataCustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
}
