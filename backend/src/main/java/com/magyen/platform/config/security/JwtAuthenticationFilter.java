package com.magyen.platform.config.security;

import com.magyen.platform.administration.application.port.AuthenticatedPrincipal;
import com.magyen.platform.administration.application.port.AuthenticatedPrincipalReconciler;
import com.magyen.platform.administration.application.port.AuthenticationTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Valida el JWT Bearer en cada request autenticada.
 * <p>
 * No registra tokens, hashes ni encabezados Authorization.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationTokenValidator authenticationTokenValidator;
    private final AuthenticatedPrincipalReconciler authenticatedPrincipalReconciler;

    public JwtAuthenticationFilter(
            AuthenticationTokenValidator authenticationTokenValidator,
            AuthenticatedPrincipalReconciler authenticatedPrincipalReconciler
    ) {
        this.authenticationTokenValidator = Objects.requireNonNull(
                authenticationTokenValidator,
                "Authentication token validator must not be null"
        );
        this.authenticatedPrincipalReconciler = Objects.requireNonNull(
                authenticatedPrincipalReconciler,
                "Authenticated principal reconciler must not be null"
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            authenticationTokenValidator.validate(token)
                    .flatMap(authenticatedPrincipalReconciler::reconcile)
                    .ifPresent(this::setAuthentication);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(AuthenticatedPrincipal principal) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
