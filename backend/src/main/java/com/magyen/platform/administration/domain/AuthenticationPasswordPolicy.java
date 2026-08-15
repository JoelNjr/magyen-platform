package com.magyen.platform.administration.domain;

import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

/**
 * Política V1 de contraseñas para usuarios internos.
 * <p>
 * No registra el valor recibido.
 */
public final class AuthenticationPasswordPolicy {

    public static final int MINIMUM_LENGTH = 8;
    public static final int MAXIMUM_LENGTH = 72;

    private AuthenticationPasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new AdministrationDomainException("Password must not be blank");
        }
        if (rawPassword.length() < MINIMUM_LENGTH) {
            throw new AdministrationDomainException(
                    "Password must be at least " + MINIMUM_LENGTH + " characters"
            );
        }
        if (rawPassword.length() > MAXIMUM_LENGTH) {
            throw new AdministrationDomainException(
                    "Password must not exceed " + MAXIMUM_LENGTH + " characters"
            );
        }
    }
}
