package com.magyen.platform.administration.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para el agregado {@link AuthenticationUser}.
 * <p>
 * La implementación concreta vivirá en la capa de infraestructura.
 */
public interface AuthenticationUserRepository {

    AuthenticationUser save(AuthenticationUser authenticationUser);

    Optional<AuthenticationUser> findById(UUID id);

    Optional<AuthenticationUser> findByUsername(String username);

    List<AuthenticationUser> findAllOrderByUsername();

    long countEnabledByRole(AuthenticationRole role);
}
