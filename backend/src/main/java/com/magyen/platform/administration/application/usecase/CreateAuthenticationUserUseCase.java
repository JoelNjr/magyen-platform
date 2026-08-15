package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticationUserView;
import com.magyen.platform.administration.application.dto.CreateAuthenticationUserCommand;
import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationPasswordPolicy;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import com.magyen.platform.administration.domain.exception.AuthenticationUsernameAlreadyExistsException;

import java.util.Objects;

/**
 * Crea un usuario interno. El password se hashea antes de persistir.
 */
public class CreateAuthenticationUserUseCase {

    private final AuthenticationUserRepository authenticationUserRepository;
    private final PasswordHasher passwordHasher;

    public CreateAuthenticationUserUseCase(
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher
    ) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher must not be null");
    }

    public AuthenticationUserView execute(CreateAuthenticationUserCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        if (command.role() == null) {
            throw new AdministrationDomainException("Role must be ADMIN or OPERATOR");
        }
        if (command.role() != AuthenticationRole.ADMIN && command.role() != AuthenticationRole.OPERATOR) {
            throw new AdministrationDomainException("Role must be ADMIN or OPERATOR");
        }

        AuthenticationPasswordPolicy.validate(command.password());

        String username = requireUsername(command.username());
        if (authenticationUserRepository.findByUsername(username).isPresent()) {
            throw new AuthenticationUsernameAlreadyExistsException();
        }

        AuthenticationUser authenticationUser = AuthenticationUser.create(
                username,
                passwordHasher.hash(command.password()),
                true,
                command.role()
        );
        AuthenticationUser saved = authenticationUserRepository.save(authenticationUser);
        return toView(saved);
    }

    private static String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new AdministrationDomainException("Username must not be blank");
        }
        return username.trim();
    }

    private static AuthenticationUserView toView(AuthenticationUser authenticationUser) {
        return new AuthenticationUserView(
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole(),
                authenticationUser.isEnabled()
        );
    }
}
