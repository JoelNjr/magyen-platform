package com.magyen.platform.administration.application.usecase;

import com.magyen.platform.administration.application.dto.AuthenticateUserCommand;
import com.magyen.platform.administration.application.dto.AuthenticateUserResult;
import com.magyen.platform.administration.application.port.AuthenticationTokenIssuer;
import com.magyen.platform.administration.application.port.IssuedAuthenticationToken;
import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AuthenticationFailedException;

import java.util.Objects;

/**
 * Autentica una identidad y emite un token de acceso.
 */
public class AuthenticateUserUseCase {

    /**
     * Hash BCrypt válido usado solo para igualar el costo de verificación
     * cuando el usuario no existe. No corresponde a ninguna cuenta real.
     */
    static final String UNKNOWN_USER_TIMING_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final AuthenticationUserRepository authenticationUserRepository;
    private final PasswordHasher passwordHasher;
    private final AuthenticationTokenIssuer authenticationTokenIssuer;

    public AuthenticateUserUseCase(
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher,
            AuthenticationTokenIssuer authenticationTokenIssuer
    ) {
        this.authenticationUserRepository = Objects.requireNonNull(
                authenticationUserRepository,
                "Authentication user repository must not be null"
        );
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "Password hasher must not be null");
        this.authenticationTokenIssuer = Objects.requireNonNull(
                authenticationTokenIssuer,
                "Authentication token issuer must not be null"
        );
    }

    public AuthenticateUserResult execute(AuthenticateUserCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        AuthenticationUser authenticationUser = authenticationUserRepository
                .findByUsername(command.username().trim())
                .orElse(null);

        String passwordHash = authenticationUser != null
                ? authenticationUser.getPasswordHash()
                : UNKNOWN_USER_TIMING_HASH;
        boolean passwordMatches = passwordHasher.matches(command.password(), passwordHash);

        if (authenticationUser == null || !authenticationUser.isEnabled() || !passwordMatches) {
            throw new AuthenticationFailedException();
        }

        IssuedAuthenticationToken issuedToken = authenticationTokenIssuer.issue(
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole()
        );

        return new AuthenticateUserResult(
                issuedToken.token(),
                "Bearer",
                issuedToken.expiresInSeconds(),
                authenticationUser.getId(),
                authenticationUser.getUsername(),
                authenticationUser.getRole()
        );
    }

    private void validateCommand(AuthenticateUserCommand command) {
        if (command.username() == null || command.username().isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (command.password() == null || command.password().isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
    }
}
