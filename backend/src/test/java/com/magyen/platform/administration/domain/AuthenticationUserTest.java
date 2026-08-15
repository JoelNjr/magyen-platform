package com.magyen.platform.administration.domain;

import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationUserTest {

    @Test
    void rejectsPlaintextPassword() {
        AdministrationDomainException exception = assertThrows(
                AdministrationDomainException.class,
                () -> AuthenticationUser.create("operator", "plaintext-password", true, AuthenticationRole.OPERATOR)
        );

        assertTrue(exception.getMessage().contains("hash"));
    }

    @Test
    void toStringDoesNotExposePasswordHash() {
        AuthenticationUser authenticationUser = AuthenticationUser.create(
                "operator",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                true,
                AuthenticationRole.OPERATOR
        );

        String text = authenticationUser.toString();
        assertFalse(text.contains("$2a$"));
        assertFalse(text.toLowerCase().contains("password"));
    }

    @Test
    void activateAndDeactivateAreIdempotent() {
        AuthenticationUser authenticationUser = AuthenticationUser.create(
                "operator",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                true,
                AuthenticationRole.OPERATOR
        );

        AuthenticationUser stillEnabled = authenticationUser.activate();
        assertSame(authenticationUser, stillEnabled);

        AuthenticationUser disabled = authenticationUser.deactivate();
        assertFalse(disabled.isEnabled());
        assertSame(disabled, disabled.deactivate());
        assertTrue(disabled.activate().isEnabled());
    }

    @Test
    void withRoleReplacesOnlyWhenDifferent() {
        AuthenticationUser authenticationUser = AuthenticationUser.create(
                "operator",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                true,
                AuthenticationRole.OPERATOR
        );

        assertSame(authenticationUser, authenticationUser.withRole(AuthenticationRole.OPERATOR));
        assertEquals(AuthenticationRole.ADMIN, authenticationUser.withRole(AuthenticationRole.ADMIN).getRole());
        assertTrue(authenticationUser.withRole(AuthenticationRole.ADMIN).isEnabledAdministrator());
    }
}
