package com.magyen.platform.administration.infrastructure.security;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationUserBootstrapTest {

    private static final String BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Mock
    private Environment environment;

    @Mock
    private AuthenticationUserRepository authenticationUserRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private AuthenticationBootstrapProperties bootstrapProperties;
    private AuthenticationUserBootstrap authenticationUserBootstrap;

    @BeforeEach
    void setUp() {
        bootstrapProperties = new AuthenticationBootstrapProperties();
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        authenticationUserBootstrap = new AuthenticationUserBootstrap(
                environment,
                bootstrapProperties,
                authenticationUserRepository,
                passwordHasher,
                transactionManager
        );
    }

    @Test
    void skipsWhenDisabled() {
        bootstrapProperties.setEnabled(false);

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        verify(authenticationUserRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void skipsWhenCredentialsAreBlank() {
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setUsername(" ");
        bootstrapProperties.setPassword(" ");

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        verify(authenticationUserRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void skipsWhenUserAlreadyExists() {
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setUsername("local-admin");
        bootstrapProperties.setPassword("change-me");
        bootstrapProperties.setRole("ADMIN");
        when(authenticationUserRepository.findByUsername("local-admin"))
                .thenReturn(Optional.of(existingUser()));

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        verify(authenticationUserRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void createsAdminAndHashesPassword() {
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setUsername("local-admin");
        bootstrapProperties.setPassword("change-me");
        bootstrapProperties.setRole("ADMIN");
        when(authenticationUserRepository.findByUsername("local-admin")).thenReturn(Optional.empty());
        when(passwordHasher.hash("change-me")).thenReturn(BCRYPT_HASH);

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        ArgumentCaptor<AuthenticationUser> captor = ArgumentCaptor.forClass(AuthenticationUser.class);
        verify(passwordHasher).hash("change-me");
        verify(authenticationUserRepository).save(captor.capture());
        AuthenticationUser saved = captor.getValue();
        assertEquals("local-admin", saved.getUsername());
        assertEquals(AuthenticationRole.ADMIN, saved.getRole());
        assertEquals(BCRYPT_HASH, saved.getPasswordHash());
        assertTrue(saved.isEnabled());
        verify(transactionManager).commit(any(TransactionStatus.class));
    }

    @Test
    void skipsInvalidRole() {
        bootstrapProperties.setEnabled(true);
        bootstrapProperties.setUsername("local-admin");
        bootstrapProperties.setPassword("change-me");
        bootstrapProperties.setRole("CLIENT");

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        verify(authenticationUserRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }

    @Test
    void enablesFromEnvironmentFallback() {
        bootstrapProperties.setEnabled(false);
        when(environment.getProperty("AUTH_BOOTSTRAP_ENABLED")).thenReturn(" true\r");
        when(environment.getProperty("AUTH_BOOTSTRAP_USERNAME")).thenReturn("env-admin");
        when(environment.getProperty("AUTH_BOOTSTRAP_PASSWORD")).thenReturn("env-password");
        when(authenticationUserRepository.findByUsername("env-admin")).thenReturn(Optional.empty());
        when(passwordHasher.hash("env-password")).thenReturn(BCRYPT_HASH);

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        ArgumentCaptor<AuthenticationUser> captor = ArgumentCaptor.forClass(AuthenticationUser.class);
        verify(authenticationUserRepository).save(captor.capture());
        assertEquals("env-admin", captor.getValue().getUsername());
        assertEquals(AuthenticationRole.ADMIN, captor.getValue().getRole());
        verify(passwordHasher).hash("env-password");
    }

    @Test
    void enablesFromRelaxedBootstrapProperty() {
        bootstrapProperties.setEnabled(false);
        lenient().when(environment.getProperty("auth.bootstrap.enabled")).thenReturn("true");
        lenient().when(environment.getProperty("auth.bootstrap.username")).thenReturn("relaxed-admin");
        lenient().when(environment.getProperty("auth.bootstrap.password")).thenReturn("relaxed-password");
        when(authenticationUserRepository.findByUsername("relaxed-admin")).thenReturn(Optional.empty());
        when(passwordHasher.hash("relaxed-password")).thenReturn(BCRYPT_HASH);

        authenticationUserBootstrap.run(new DefaultApplicationArguments());

        ArgumentCaptor<AuthenticationUser> captor = ArgumentCaptor.forClass(AuthenticationUser.class);
        verify(authenticationUserRepository).save(captor.capture());
        assertEquals("relaxed-admin", captor.getValue().getUsername());
        assertEquals(AuthenticationRole.ADMIN, captor.getValue().getRole());
    }

    private static AuthenticationUser existingUser() {
        return AuthenticationUser.create(
                "local-admin",
                BCRYPT_HASH,
                true,
                AuthenticationRole.ADMIN
        );
    }
}
