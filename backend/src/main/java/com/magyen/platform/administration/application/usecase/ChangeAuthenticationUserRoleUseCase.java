package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.ChangeAuthenticationUserRoleCommand;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

import java.util.Objects;

/**
 * Cambia el rol interno de un usuario. Protege al último ADMIN activo.
 */
public class ChangeAuthenticationUserRoleUseCase {

    static final String LAST_ACTIVE_ADMINISTRATOR_MESSAGE =
            "The last active administrator cannot be demoted.";

    private final AuthenticationUserRepository authenticationUserRepository;

    public ChangeAuthenticationUserRoleUseCase(AuthenticationUserRepository authenticationUserRepository) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
    }

    public AuthenticationUserView execute(ChangeAuthenticationUserRoleCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.userId(), "User id must not be null");
        if (command.role() == null) {
            throw new AdministrationDomainException("Role must be ADMIN or OPERATOR");
        }

        AuthenticationUser authenticationUser = authenticationUserRepository.findById(command.userId())
                .orElseThrow(() -> new AdministrationDomainException("Authentication user was not found."));

        if (authenticationUser.isEnabledAdministrator()
                && command.role() == AuthenticationRole.OPERATOR
                && authenticationUserRepository.countEnabledByRole(AuthenticationRole.ADMIN) <= 1) {
            throw new AdministrationDomainException(LAST_ACTIVE_ADMINISTRATOR_MESSAGE);
        }

        AuthenticationUser updated = authenticationUser.withRole(command.role());
        AuthenticationUser saved = authenticationUserRepository.save(updated);
        return new AuthenticationUserView(
                saved.getId(),
                saved.getUsername(),
                saved.getRole(),
                saved.isEnabled()
        );
    }
}
