package com.magyen.platform.administration.application.port;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

/**
 * Port que emite tokens de autenticación.
 */
public interface AuthenticationTokenIssuer {

    IssuedAuthenticationToken issue(UUID userId, String username, AuthenticationRole role);
}
