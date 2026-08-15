package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.port.AuthenticatedPrincipal;
import com.magyen.platform.administration.application.port.AuthenticatedPrincipalReconciler;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Ajusta el principal del JWT con enabled y rol persistidos.
 */
public class ReconcileAuthenticatedPrincipalUseCase implements AuthenticatedPrincipalReconciler {

    private final AuthenticationUserRepository authenticationUserRepository;

    public ReconcileAuthenticatedPrincipalUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    @Override
    public Optional<AuthenticatedPrincipal> reconcile(AuthenticatedPrincipal tokenPrincipal) {
        Objects.requireNonNull(tokenPrincipal, "Token principal must not be null");
        Objects.requireNonNull(tokenPrincipal.userId(), "User id must not be null");

        Optional<AuthenticationUser> persistedUser = authenticationUserRepository.findById(tokenPrincipal.userId());
        if (persistedUser.isEmpty() || !persistedUser.get().isEnabled()) {
            return Optional.empty();
        }

        AuthenticationUser authenticationUser = persistedUser.get();
        return Optional.of(new AuthenticatedPrincipal(
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole()
        ));
    }
}
