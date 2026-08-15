package com.magyen.platform.administration.infrastructure.persistence.mapper;

import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.infrastructure.persistence.entity.AuthenticationUserEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link AuthenticationUser} y su modelo JPA.
 */
public class AuthenticationUserPersistenceMapper {

    public AuthenticationUserEntity toEntity(AuthenticationUser authenticationUser) {
        Objects.requireNonNull(authenticationUser, "Authentication user must not be null");

        AuthenticationUserEntity authenticationUserEntity = new AuthenticationUserEntity();
        authenticationUserEntity.setId(authenticationUser.getId());
        authenticationUserEntity.setUsername(authenticationUser.getUsername());
        authenticationUserEntity.setPasswordHash(authenticationUser.getPasswordHash());
        authenticationUserEntity.setEnabled(authenticationUser.isEnabled());
        authenticationUserEntity.setRole(authenticationUser.getRole());
        return authenticationUserEntity;
    }

    public AuthenticationUser toDomain(AuthenticationUserEntity authenticationUserEntity) {
        Objects.requireNonNull(authenticationUserEntity, "Authentication user entity must not be null");

        return AuthenticationUser.reconstitute(
                authenticationUserEntity.getId(),
                authenticationUserEntity.getUsername(),
                authenticationUserEntity.getPasswordHash(),
                authenticationUserEntity.isEnabled(),
                authenticationUserEntity.getRole()
        );
    }
}
