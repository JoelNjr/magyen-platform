package com.magyen.platform.administration.infrastructure.security;

import com.magyen.platform.administration.application.port.AuthenticatedPrincipal;
import com.magyen.platform.administration.application.port.AuthenticationTokenIssuer;
import com.magyen.platform.administration.application.port.AuthenticationTokenValidator;
import com.magyen.platform.administration.application.port.IssuedAuthenticationToken;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Emite y valida JWT HMAC-SHA256. Los secretos nunca se registran en logs.
 */
public class NimbusJwtTokenAdapter implements AuthenticationTokenIssuer, AuthenticationTokenValidator {

    private static final int MINIMUM_SECRET_LENGTH = 32;
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLE_CLAIM = "role";

    private final byte[] secret;
    private final long expirationMs;
    private final Clock clock;

    public NimbusJwtTokenAdapter(String secret, long expirationMs, Clock clock) {
        Objects.requireNonNull(secret, "JWT secret must not be null");
        if (secret.length() < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException("JWT secret must be at least " + MINIMUM_SECRET_LENGTH + " characters");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public IssuedAuthenticationToken issue(UUID userId, String username, AuthenticationRole role) {
        Objects.requireNonNull(userId, "User id must not be null");
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(role, "Role must not be null");

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim(USERNAME_CLAIM, username)
                .claim(ROLE_CLAIM, role.name())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        try {
            signedJwt.sign(new MACSigner(secret));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign authentication token", exception);
        }

        return new IssuedAuthenticationToken(
                signedJwt.serialize(),
                expiresAt,
                expirationMs / 1000
        );
    }

    @Override
    public Optional<AuthenticatedPrincipal> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            boolean signatureValid = signedJwt.verify(new MACVerifier(secret));
            if (!signatureValid) {
                return Optional.empty();
            }

            JWTClaimsSet claimsSet = signedJwt.getJWTClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime == null || expirationTime.toInstant().isBefore(clock.instant())) {
                return Optional.empty();
            }

            UUID userId = UUID.fromString(claimsSet.getSubject());
            String username = claimsSet.getStringClaim(USERNAME_CLAIM);
            String roleName = claimsSet.getStringClaim(ROLE_CLAIM);
            if (username == null || username.isBlank() || roleName == null || roleName.isBlank()) {
                return Optional.empty();
            }

            AuthenticationRole role = AuthenticationRole.valueOf(roleName);
            return Optional.of(new AuthenticatedPrincipal(userId, username, role));
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
