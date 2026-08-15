package com.magyen.platform.administration.presentation;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.administration.domain.exception.AuthenticationFailedException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationUserRepository authenticationUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JsonMapper jsonMapper;

    @Value("${magyen.security.jwt.secret}")
    private String jwtSecret;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void loginSucceedsAndDoesNotExposePasswordHash() throws Exception {
        String username = uniqueUsername("login-ok");
        String password = "correct-password";
        saveUser(username, password, true);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(username, password))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$").value(not(org.hamcrest.Matchers.hasKey("passwordHash"))));
    }

    @Test
    void loginRemainsAccessibleWithoutPriorAuthentication() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "missing-user",
                                          "password": "any-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(AuthenticationFailedException.DEFAULT_MESSAGE));
    }

    @Test
    void malformedLoginRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "operator"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void validTokenIsAcceptedByProtectedEndpoint() throws Exception {
        String username = uniqueUsername("token-ok");
        String password = "correct-password";
        AuthenticationUser saved = saveUser(username, password, true);
        String accessToken = loginAndExtractToken(username, password);

        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(saved.getId().toString()))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        String username = uniqueUsername("expired");
        AuthenticationUser saved = saveUser(username, "correct-password", true);
        String expiredToken = signedToken(saved, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(60));

        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private AuthenticationUser saveUser(String username, String rawPassword, boolean enabled) {
        return authenticationUserRepository.save(AuthenticationUser.create(
                username,
                passwordHasher.hash(rawPassword),
                enabled,
                AuthenticationRole.OPERATOR
        ));
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(username, password))
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private String signedToken(AuthenticationUser authenticationUser, Instant issuedAt, Instant expiresAt)
            throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(authenticationUser.getId().toString())
                .claim("username", authenticationUser.getUsername())
                .claim("role", authenticationUser.getRole().name())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJwt.sign(new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8)));
        return signedJwt.serialize();
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
