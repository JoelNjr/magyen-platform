package com.magyen.platform.administration.application.port;

/**
 * Port para hashear y verificar contraseñas.
 * <p>
 * La implementación pertenece a infraestructura y usa el algoritmo de Spring Security.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
