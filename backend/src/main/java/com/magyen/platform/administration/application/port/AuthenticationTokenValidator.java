package com.magyen.platform.administration.application.port;

import java.util.Optional;

/**
 * Port que valida tokens de autenticación.
 */
public interface AuthenticationTokenValidator {

    Optional<AuthenticatedPrincipal> validate(String token);
}
