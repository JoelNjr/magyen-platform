package com.magyen.platform.config.security;

import com.magyen.platform.administration.application.port.AuthenticatedPrincipalReconciler;
import com.magyen.platform.administration.application.port.AuthenticationTokenValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Autenticación JWT stateless y autorización V1 por rol interno.
 * <p>
 * CSRF se desactiva porque el SPA envía Bearer JWT y no usa cookies de sesión.
 * Las reglas de rol viven aquí, no en el dominio de negocio.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            throw new AuthenticationServiceException("Use POST /api/v1/auth/login");
        };
    }

    @Bean
    public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return new JsonAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    public JsonAccessDeniedHandler jsonAccessDeniedHandler(JsonMapper jsonMapper) {
        return new JsonAccessDeniedHandler(jsonMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationTokenValidator authenticationTokenValidator,
            AuthenticatedPrincipalReconciler authenticatedPrincipalReconciler,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            JsonAccessDeniedHandler jsonAccessDeniedHandler
    ) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                authenticationTokenValidator,
                authenticatedPrincipalReconciler
        );

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .accessDeniedHandler(jsonAccessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/finance/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/inventory/*/unit-cost").hasRole("ADMIN")
                        .requestMatchers("/api/v1/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/notifications").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
