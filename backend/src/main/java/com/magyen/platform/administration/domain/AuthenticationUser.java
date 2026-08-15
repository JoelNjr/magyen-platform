package com.magyen.platform.administration.domain;

import com.magyen.platform.administration.domain.exception.AdministrationDomainException;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de identidad de autenticación.
 * <p>
 * Contiene únicamente datos necesarios para autenticar. No modela empleados,
 * clientes ni perfiles de negocio.
 */
public class AuthenticationUser {

    private static final int MAX_USERNAME_LENGTH = 100;

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final AuthenticationRole role;

    private AuthenticationUser(
            UUID id,
            String username,
            String passwordHash,
            boolean enabled,
            AuthenticationRole role
    ) {
        this.id = Objects.requireNonNull(id, "Authentication user id must not be null");
        this.username = requireUsername(username);
        this.passwordHash = requirePasswordHash(passwordHash);
        this.enabled = enabled;
        this.role = Objects.requireNonNull(role, "Authentication role must not be null");
    }

    /**
     * Crea una identidad de autenticación. El hash de contraseña ya debe estar calculado.
     */
    public static AuthenticationUser create(
            String username,
            String passwordHash,
            boolean enabled,
            AuthenticationRole role
    ) {
        return new AuthenticationUser(UUID.randomUUID(), username, passwordHash, enabled, role);
    }

    /**
     * Reconstruye la identidad desde persistencia. No aplica lógica de creación.
     */
    public static AuthenticationUser reconstitute(
            UUID id,
            String username,
            String passwordHash,
            boolean enabled,
            AuthenticationRole role
    ) {
        return new AuthenticationUser(id, username, passwordHash, enabled, role);
    }

    public AuthenticationUser activate() {
        if (enabled) {
            return this;
        }
        return new AuthenticationUser(id, username, passwordHash, true, role);
    }

    public AuthenticationUser deactivate() {
        if (!enabled) {
            return this;
        }
        return new AuthenticationUser(id, username, passwordHash, false, role);
    }

    public AuthenticationUser withRole(AuthenticationRole newRole) {
        Objects.requireNonNull(newRole, "Authentication role must not be null");
        if (role == newRole) {
            return this;
        }
        return new AuthenticationUser(id, username, passwordHash, enabled, newRole);
    }

    public boolean isEnabledAdministrator() {
        return enabled && role == AuthenticationRole.ADMIN;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AuthenticationRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AuthenticationUser authenticationUser = (AuthenticationUser) other;
        return id.equals(authenticationUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuthenticationUser{id=" + id + ", username='" + username + "', enabled=" + enabled + ", role=" + role + "}";
    }

    private static String requireUsername(String username) {
        Objects.requireNonNull(username, "Username must not be null");
        String normalizedUsername = username.trim();
        if (normalizedUsername.isBlank()) {
            throw new AdministrationDomainException("Username must not be blank");
        }
        if (normalizedUsername.length() > MAX_USERNAME_LENGTH) {
            throw new AdministrationDomainException("Username must not exceed " + MAX_USERNAME_LENGTH + " characters");
        }
        return normalizedUsername;
    }

    private static String requirePasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "Password hash must not be null");
        if (passwordHash.isBlank()) {
            throw new AdministrationDomainException("Password hash must not be blank");
        }
        if (passwordHash.equals(passwordHash.trim()) && looksLikePlaintextPassword(passwordHash)) {
            throw new AdministrationDomainException("Password must be stored as a hash");
        }
        return passwordHash;
    }

    private static boolean looksLikePlaintextPassword(String value) {
        return !value.startsWith("$2a$")
                && !value.startsWith("$2b$")
                && !value.startsWith("$2y$");
    }
}
