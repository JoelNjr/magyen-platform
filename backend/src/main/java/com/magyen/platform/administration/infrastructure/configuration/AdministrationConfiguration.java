package com.magyen.platform.administration.infrastructure.configuration;

import com.magyen.platform.administration.application.port.AuthenticatedPrincipalReconciler;
import com.magyen.platform.administration.application.port.AuthenticationTokenIssuer;
import com.magyen.platform.administration.application.port.AuthenticationTokenValidator;
import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.application.usecase.ActivateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.AuthenticateUserUseCase;
import com.magyen.platform.administration.application.usecase.ChangeAuthenticationUserRoleUseCase;
import com.magyen.platform.administration.application.usecase.CreateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.DeactivateAuthenticationUserUseCase;
import com.magyen.platform.administration.application.usecase.GetAuthenticatedUserUseCase;
import com.magyen.platform.administration.application.usecase.ListAuthenticationUsersUseCase;
import com.magyen.platform.administration.application.usecase.ReconcileAuthenticatedPrincipalUseCase;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.infrastructure.persistence.mapper.AuthenticationUserPersistenceMapper;
import com.magyen.platform.administration.infrastructure.security.AuthenticationBootstrapProperties;
import com.magyen.platform.administration.infrastructure.security.AuthenticationUserBootstrap;
import com.magyen.platform.administration.infrastructure.security.BcryptPasswordHasher;
import com.magyen.platform.administration.infrastructure.security.NimbusJwtTokenAdapter;
import com.magyen.platform.administration.presentation.auth.mapper.AuthPresentationMapper;
import com.magyen.platform.administration.presentation.user.mapper.AuthenticationUserAdminPresentationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

/**
 * Ensambla los beans del módulo de administración que no se registran por estereotipos Spring.
 */
@Configuration
@EnableConfigurationProperties(AuthenticationBootstrapProperties.class)
public class AdministrationConfiguration {

    @Bean
    public AuthPresentationMapper authPresentationMapper() {
        return new AuthPresentationMapper();
    }

    @Bean
    public AuthenticationUserAdminPresentationMapper authenticationUserAdminPresentationMapper() {
        return new AuthenticationUserAdminPresentationMapper();
    }

    @Bean
    public AuthenticationUserPersistenceMapper authenticationUserPersistenceMapper() {
        return new AuthenticationUserPersistenceMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return new BcryptPasswordHasher(passwordEncoder);
    }

    @Bean
    public NimbusJwtTokenAdapter nimbusJwtTokenAdapter(
            @Value("${magyen.security.jwt.secret}") String jwtSecret,
            @Value("${magyen.security.jwt.expiration-ms}") long jwtExpirationMs
    ) {
        return new NimbusJwtTokenAdapter(jwtSecret, jwtExpirationMs, Clock.systemUTC());
    }

    @Bean
    public AuthenticationTokenIssuer authenticationTokenIssuer(NimbusJwtTokenAdapter nimbusJwtTokenAdapter) {
        return nimbusJwtTokenAdapter;
    }

    @Bean
    public AuthenticationTokenValidator authenticationTokenValidator(NimbusJwtTokenAdapter nimbusJwtTokenAdapter) {
        return nimbusJwtTokenAdapter;
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher,
            AuthenticationTokenIssuer authenticationTokenIssuer
    ) {
        return new AuthenticateUserUseCase(
                authenticationUserRepository,
                passwordHasher,
                authenticationTokenIssuer
        );
    }

    @Bean
    public GetAuthenticatedUserUseCase getAuthenticatedUserUseCase(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new GetAuthenticatedUserUseCase(authenticationUserRepository);
    }

    @Bean
    public AuthenticatedPrincipalReconciler authenticatedPrincipalReconciler(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new ReconcileAuthenticatedPrincipalUseCase(authenticationUserRepository);
    }

    @Bean
    public ListAuthenticationUsersUseCase listAuthenticationUsersUseCase(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new ListAuthenticationUsersUseCase(authenticationUserRepository);
    }

    @Bean
    public CreateAuthenticationUserUseCase createAuthenticationUserUseCase(
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher
    ) {
        return new CreateAuthenticationUserUseCase(authenticationUserRepository, passwordHasher);
    }

    @Bean
    public ActivateAuthenticationUserUseCase activateAuthenticationUserUseCase(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new ActivateAuthenticationUserUseCase(authenticationUserRepository);
    }

    @Bean
    public DeactivateAuthenticationUserUseCase deactivateAuthenticationUserUseCase(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new DeactivateAuthenticationUserUseCase(authenticationUserRepository);
    }

    @Bean
    public ChangeAuthenticationUserRoleUseCase changeAuthenticationUserRoleUseCase(
            AuthenticationUserRepository authenticationUserRepository
    ) {
        return new ChangeAuthenticationUserRoleUseCase(authenticationUserRepository);
    }

    @Bean
    public AuthenticationUserBootstrap authenticationUserBootstrap(
            Environment environment,
            AuthenticationBootstrapProperties authenticationBootstrapProperties,
            AuthenticationUserRepository authenticationUserRepository,
            PasswordHasher passwordHasher,
            PlatformTransactionManager transactionManager
    ) {
        return new AuthenticationUserBootstrap(
                environment,
                authenticationBootstrapProperties,
                authenticationUserRepository,
                passwordHasher,
                transactionManager
        );
    }
}
