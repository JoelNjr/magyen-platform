package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.GetAuthenticatedUserQuery;
import com.magyen.platform.administration.application.dto.GetAuthenticatedUserResult;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AuthenticationFailedException;

import java.util.Objects;

/**
 * Obtiene la identidad autenticada requerida por el adaptador de seguridad.
 */
public class GetAuthenticatedUserUseCase {

    private final AuthenticationUserRepository authenticationUserRepository;

    public GetAuthenticatedUserUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    public GetAuthenticatedUserResult execute(GetAuthenticatedUserQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.userId(), "User id must not be null");

        AuthenticationUser authenticationUser = authenticationUserRepository.findById(query.userId())
                .orElseThrow(AuthenticationFailedException::new);

        if (!authenticationUser.isEnabled()) {
            throw new AuthenticationFailedException();
        }

        return new GetAuthenticatedUserResult(
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole(),
                authenticationUser.isEnabled()
        );
    }
}
