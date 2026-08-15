package com.magyen.platform.administration.domain;

import com.magyen.platform.administration.domain.exception.AdministrationDomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationPasswordPolicyTest {

    @Test
    void acceptsPasswordThatMeetsMinimumLength() {
        assertDoesNotThrow(() -> AuthenticationPasswordPolicy.validate("12345678"));
    }

    @Test
    void rejectsBlankPassword() {
        AdministrationDomainException exception = assertThrows(
                AdministrationDomainException.class,
                () -> AuthenticationPasswordPolicy.validate("   ")
        );
        assertTrue(exception.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void rejectsShortPassword() {
        AdministrationDomainException exception = assertThrows(
                AdministrationDomainException.class,
                () -> AuthenticationPasswordPolicy.validate("short")
        );
        assertTrue(exception.getMessage().contains("8"));
    }
}
