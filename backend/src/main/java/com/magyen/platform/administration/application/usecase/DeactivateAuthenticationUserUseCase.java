package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.DeactivateAuthenticationUserCommand;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

import java.util.Objects;

/**
 * Desactiva un usuario interno. Protege al último ADMIN activo.
 */
public class DeactivateAuthenticationUserUseCase {

    static final String LAST_ACTIVE_ADMINISTRATOR_MESSAGE =
            "The last active administrator cannot be deactivated.";

    private final AuthenticationUserRepository authenticationUserRepository;

    public DeactivateAuthenticationUserUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    public AuthenticationUserView execute(DeactivateAuthenticationUserCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.userId(), "User id must not be null");

        AuthenticationUser authenticationUser = authenticationUserRepository.findById(command.userId())
                .orElseThrow(() -> new AdministrationDomainException("Authentication user was not found."));

        if (authenticationUser.isEnabledAdministrator()
                && authenticationUserRepository.countEnabledByRole(AuthenticationRole.ADMIN) <= 1) {
            throw new AdministrationDomainException(LAST_ACTIVE_ADMINISTRATOR_MESSAGE);
        }

        AuthenticationUser deactivated = authenticationUser.deactivate();
        AuthenticationUser saved = authenticationUserRepository.save(deactivated);
        return new AuthenticationUserView(
                saved.getId(),
                saved.getUsername(),
                saved.getRole(),
                saved.isEnabled()
        );
    }
}
