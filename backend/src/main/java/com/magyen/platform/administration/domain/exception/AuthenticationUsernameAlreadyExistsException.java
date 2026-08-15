package com.magyen.platform.administration.domain.exception;

/**
 * El username de autenticación ya está en uso.
 */
public class AuthenticationUsernameAlreadyExistsException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Username already exists.";

    public AuthenticationUsernameAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
}
