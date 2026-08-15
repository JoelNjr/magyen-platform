package com.magyen.platform.administration.domain.exception;

/**
 * Fallo de autenticación con mensaje seguro.
 * <p>
 * No revela si el usuario existe, está deshabilitado o si la contraseña es incorrecta.
 */
public class AuthenticationFailedException extends AdministrationDomainException {

    public static final String DEFAULT_MESSAGE = "Invalid credentials.";

    public AuthenticationFailedException() {
        super(DEFAULT_MESSAGE);
    }
}
