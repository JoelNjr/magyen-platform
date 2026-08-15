package com.magyen.platform.administration.infrastructure.security;

import com.magyen.platform.administration.application.port.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

/**
 * Adaptador BCrypt de Spring Security para el port {@link PasswordHasher}.
 */
public class BcryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "Password encoder must not be null");
    }

    @Override
    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "Raw password must not be null");
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        Objects.requireNonNull(rawPassword, "Raw password must not be null");
        Objects.requireNonNull(passwordHash, "Password hash must not be null");
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
