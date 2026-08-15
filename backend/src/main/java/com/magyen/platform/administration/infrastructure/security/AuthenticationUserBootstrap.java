package com.magyen.platform.administration.infrastructure.security;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Crea el primer usuario interno cuando el bootstrap está habilitado.
 * <p>
 * No registra contraseñas, hashes ni secretos.
 */
public class AuthenticationUserBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationUserBootstrap.class);

    private final Environment environment;
    private final AuthenticationBootstrapProperties bootstrapProperties;
    private final AuthenticationUserRepository authenticationUserRepository;
    private final PasswordHasher passwordHasher;
    private final TransactionTemplate transactionTemplate;

    public AuthenticationUserBootstrap(
            Environment environment,
            AuthenticationBootstrapProperties bootstrapProperties,
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher,
            PlatformTransactionManager transactionManager
    ) {
        this.environment = environment;
        this.bootstrapProperties = bootstrapProperties;
        this.authenticationUserRepository = authenticationUserRepository;
        this.passwordHasher = passwordHasher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isEnabled()) {
            LOGGER.info("Authentication bootstrap is disabled");
            return;
        }

        String username = firstNonBlank(
                bootstrapProperties.getUsername(),
                environmentValue("AUTH_BOOTSTRAP_USERNAME", "auth.bootstrap.username")
        ).trim();
        String password = firstNonBlank(
                bootstrapProperties.getPassword(),
                environmentValue("AUTH_BOOTSTRAP_PASSWORD", "auth.bootstrap.password")
        ).trim();
        String roleName = firstNonBlank(
                bootstrapProperties.getRole(),
                environmentValue("AUTH_BOOTSTRAP_ROLE", "auth.bootstrap.role"),
                AuthenticationRole.ADMIN.name()
        ).trim();

        if (username.isBlank() || password.isBlank()) {
            LOGGER.warn("Authentication bootstrap is enabled but username or password is blank; skipping");
            return;
        }

        AuthenticationRole role;
        try {
            role = AuthenticationRole.valueOf(roleName.trim());
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Authentication bootstrap role is invalid; skipping");
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (authenticationUserRepository.findByUsername(username.trim()).isPresent()) {
                LOGGER.info("Authentication bootstrap user already exists; skipping");
                return;
            }

            AuthenticationUser authenticationUser = AuthenticationUser.create(
                    username,
                    passwordHasher.hash(password),
                    true,
                    role
            );
            authenticationUserRepository.save(authenticationUser);
            LOGGER.info("Authentication bootstrap user created with role {}", role.name());
        });
    }

    private boolean isEnabled() {
        if (bootstrapProperties.isEnabled()) {
            return true;
        }
        return parseFlag(environmentValue(
                "AUTH_BOOTSTRAP_ENABLED",
                "auth.bootstrap.enabled",
                "magyen.security.bootstrap.enabled"
        ));
    }

    private String environmentValue(String... keys) {
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static boolean parseFlag(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String normalized = raw.trim().replace("\"", "").replace("'", "");
        return Boolean.parseBoolean(normalized);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
