package com.magyen.platform.administration.infrastructure.persistence.repository;

import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.infrastructure.persistence.entity.AuthenticationUserEntity;
import com.magyen.platform.administration.infrastructure.persistence.mapper.AuthenticationUserPersistenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el port {@link AuthenticationUserRepository}.
 */
@Repository
public class JpaAuthenticationUserRepository implements AuthenticationUserRepository {

    private final SpringDataAuthenticationUserJpaRepository springDataAuthenticationUserJpaRepository;
    private final AuthenticationUserPersistenceMapper authenticationUserPersistenceMapper;

    public JpaAuthenticationUserRepository(
            SpringDataAuthenticationUserJpaRepository springDataAuthenticationUserJpaRepository,
            AuthenticationUserPersistenceMapper authenticationUserPersistenceMapper
    ) {
        this.springDataAuthenticationUserJpaRepository = Objects.requireNonNull(
                springDataAuthenticationUserJpaRepository,
                "Spring Data authentication user JPA repository must not be null"
        );
        this.authenticationUserPersistenceMapper = Objects.requireNonNull(
                authenticationUserPersistenceMapper,
                "Authentication user persistence mapper must not be null"
        );
    }

    @Override
    @Transactional
    public AuthenticationUser save(AuthenticationUser authenticationUser) {
        Objects.requireNonNull(authenticationUser, "Authentication user must not be null");

        AuthenticationUserEntity authenticationUserEntity =
                authenticationUserPersistenceMapper.toEntity(authenticationUser);
        AuthenticationUserEntity savedAuthenticationUserEntity =
                springDataAuthenticationUserJpaRepository.save(authenticationUserEntity);
        return authenticationUserPersistenceMapper.toDomain(savedAuthenticationUserEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationUser> findById(UUID id) {
        Objects.requireNonNull(id, "Authentication user id must not be null");

        return springDataAuthenticationUserJpaRepository.findById(id)
                .map(authenticationUserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationUser> findByUsername(String username) {
        Objects.requireNonNull(username, "Username must not be null");

        return springDataAuthenticationUserJpaRepository.findByUsername(username)
                .map(authenticationUserPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthenticationUser> findAllOrderByUsername() {
        return springDataAuthenticationUserJpaRepository.findAllByOrderByUsernameAsc().stream()
                .map(authenticationUserPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnabledByRole(AuthenticationRole role) {
        Objects.requireNonNull(role, "Authentication role must not be null");
        return springDataAuthenticationUserJpaRepository.countByRoleAndEnabledTrue(role);
    }
}
