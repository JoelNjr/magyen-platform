package com.magyen.platform.administration.infrastructure.persistence.repository;

import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.infrastructure.persistence.entity.AuthenticationUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link AuthenticationUserEntity}.
 * <p>
 * Detalle técnico de infraestructura; no debe usarse fuera de esta capa.
 */
public interface SpringDataAuthenticationUserJpaRepository extends JpaRepository<AuthenticationUserEntity, UUID> {

    Optional<AuthenticationUserEntity> findByUsername(String username);

    List<AuthenticationUserEntity> findAllByOrderByUsernameAsc();

    long countByRoleAndEnabledTrue(AuthenticationRole role);
}
