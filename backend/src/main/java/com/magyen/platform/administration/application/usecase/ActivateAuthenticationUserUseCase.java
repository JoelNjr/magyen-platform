package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.ActivateAuthenticationUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

import java.util.Objects;

/**
 * Activa un usuario interno. Idempotente si ya está habilitado.
 */
public class ActivateAuthenticationUserUseCase {

    private final AuthenticationUserRepository authenticationUserRepository;

    public ActivateAuthenticationUserUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    public AuthenticationUserView execute(ActivateAuthenticationUserCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.userId(), "User id must not be null");

        AuthenticationUser authenticationUser = authenticationUserRepository.findById(command.userId())
                .orElseThrow(() -> new AdministrationDomainException("Authentication user was not found."));

        AuthenticationUser activated = authenticationUser.activate();
        AuthenticationUser saved = authenticationUserRepository.save(activated);
        return new AuthenticationUserView(
                saved.getId(),
                saved.getUsername(),
                saved.getRole(),
                saved.isEnabled()
        );
    }
}
