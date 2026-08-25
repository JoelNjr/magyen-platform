package com.magyen.platform.administration.presentation;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthorizationApiContractTest {

    private static final UUID UNKNOWN_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

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
    void adminCanAccessAuthorizedV1Endpoints() throws Exception {
        String accessToken = loginAs(AuthenticationRole.ADMIN);

        mockMvc.perform(authorized(get("/api/v1/auth/me"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/home/dashboard"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/customers"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/quotations"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/quotations/" + UNKNOWN_ID + "/pdf"), accessToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authorized(get("/api/v1/orders"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/orders/" + UNKNOWN_ID + "/remission/pdf"), accessToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authorized(get("/api/v1/production-orders"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/inventory"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/plotter/jobs"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/orders/profitability"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/plotter/profitability"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/finance/transactions"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/performance"), accessToken))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/" + UNKNOWN_ID + "/deductions"), accessToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authorized(get("/api/v1/finance/summary"), accessToken)
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-08-31"))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/reports/sales"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/admin/users"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/admin/catalogs"), accessToken)).andExpect(status().isOk());
    }

    @Test
    void operatorCanAccessOperationalEndpoints() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(get("/api/v1/auth/me"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/customers"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/quotations"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/quotations/" + UNKNOWN_ID + "/pdf"), accessToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authorized(get("/api/v1/orders"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/orders/" + UNKNOWN_ID + "/remission/pdf"), accessToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authorized(get("/api/v1/production-orders"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/production/labor-operators"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/inventory"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/plotter/jobs"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/orders/profitability"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/plotter/profitability"), accessToken)).andExpect(status().isOk());
        mockMvc.perform(authorized(get("/api/v1/commercial-catalogs"), accessToken)).andExpect(status().isOk());
    }

    @Test
    void operatorIsForbiddenFromHomeDashboard() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(get("/api/v1/home/dashboard"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."))
                .andExpect(jsonPath("$.financialSummary").doesNotExist())
                .andExpect(jsonPath("$.receivables").doesNotExist())
                .andExpect(jsonPath("$.completedReceivables").doesNotExist())
                .andExpect(jsonPath("$.commitments").doesNotExist())
                .andExpect(jsonPath("$.productionSummary").doesNotExist())
                .andExpect(jsonPath("$.profitabilitySummary").doesNotExist());
    }

    @Test
    void operatorIsForbiddenFromFinanceAdministration() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(get("/api/v1/finance/transactions"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."));
        mockMvc.perform(authorized(post("/api/v1/finance/transactions"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/finance/summary"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/admin/catalogs"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(patch("/api/v1/finance/obligation-occurrences/" + UNKNOWN_ID + "/cancel"), accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCannotChangeInventoryUnitCost() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(patch("/api/v1/inventory/" + UNKNOWN_ID + "/unit-cost"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "unitCost": 10
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void operatorCannotManagePayrollEmployees() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(authorized(post("/api/v1/finance/payroll/employees"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "No autorizado",
                                  "compensationType": "PRODUCTION_BASED"
                                }
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/" + UNKNOWN_ID + "/deductions"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/performance"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/" + UNKNOWN_ID + "/summary"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(get("/api/v1/finance/payroll/employees/" + UNKNOWN_ID + "/commissions"), accessToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(authorized(post("/api/v1/finance/payroll/employees/" + UNKNOWN_ID + "/deductions"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "LOAN",
                                  "amount": 10000,
                                  "deductionDate": "2026-08-17"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCannotPayPayroll() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(patch("/api/v1/finance/payroll/periods/" + UNKNOWN_ID + "/pay"), accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCannotPayRecurringFinancialObligations() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(patch("/api/v1/finance/obligation-occurrences/" + UNKNOWN_ID + "/pay"), accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get("/api/v1/quotations/" + UNKNOWN_ID + "/pdf"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get("/api/v1/orders/" + UNKNOWN_ID + "/remission/pdf"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(put("/api/v1/production-orders/" + UNKNOWN_ID + "/reference-image"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get("/api/v1/production-orders/" + UNKNOWN_ID + "/reference-image"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(delete("/api/v1/production-orders/" + UNKNOWN_ID + "/reference-image"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void authenticatedUserWithoutPermissionIsForbidden() throws Exception {
        String accessToken = loginAs(AuthenticationRole.OPERATOR);

        mockMvc.perform(authorized(get("/api/v1/reports/sales"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void loginRemainsPublic() throws Exception {
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
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidJwtRemainsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void expiredJwtRemainsUnauthorized() throws Exception {
        AuthenticationUser authenticationUser = saveUser(AuthenticationRole.ADMIN);
        String expiredToken = signedToken(
                authenticationUser,
                Instant.now().minusSeconds(3600),
                Instant.now().minusSeconds(60)
        );

        mockMvc.perform(authorized(get("/api/v1/home/dashboard"), expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tamperedRoleClaimCannotBypassAuthorization() throws Exception {
        String operatorToken = loginAs(AuthenticationRole.OPERATOR);
        String tamperedToken = replacePayloadRole(operatorToken, "OPERATOR", "ADMIN");

        mockMvc.perform(authorized(get("/api/v1/finance/transactions"), tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unknownRoleClaimIsUnauthorized() throws Exception {
        AuthenticationUser authenticationUser = saveUser(AuthenticationRole.OPERATOR);
        String unknownRoleToken = signedTokenWithRole(authenticationUser, "CLIENT");

        mockMvc.perform(authorized(get("/api/v1/home/dashboard"), unknownRoleToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private String loginAs(AuthenticationRole role) throws Exception {
        String username = uniqueUsername(role.name().toLowerCase());
        String password = "correct-password";
        saveUser(username, password, role);
        return loginAndExtractToken(username, password);
    }

    private AuthenticationUser saveUser(AuthenticationRole role) {
        return saveUser(uniqueUsername(role.name().toLowerCase()), "correct-password", role);
    }

    private AuthenticationUser saveUser(String username, String rawPassword, AuthenticationRole role) {
        return authenticationUserRepository.save(AuthenticationUser.create(
                username,
                passwordHasher.hash(rawPassword),
                true,
                role
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

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authorized(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String accessToken
    ) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private String signedToken(AuthenticationUser authenticationUser, Instant issuedAt, Instant expiresAt)
            throws Exception {
        return signedToken(authenticationUser, authenticationUser.getRole().name(), issuedAt, expiresAt);
    }

    private String signedTokenWithRole(AuthenticationUser authenticationUser, String roleName) throws Exception {
        Instant now = Instant.now();
        return signedToken(authenticationUser, roleName, now, now.plusSeconds(3600));
    }

    private String signedToken(
            AuthenticationUser authenticationUser,
            String roleName,
            Instant issuedAt,
            Instant expiresAt
    ) throws Exception {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(authenticationUser.getId().toString())
                .claim("username", authenticationUser.getUsername())
                .claim("role", roleName)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJwt.sign(new MACSigner(jwtSecret.getBytes(StandardCharsets.UTF_8)));
        return signedJwt.serialize();
    }

    private static String replacePayloadRole(String token, String fromRole, String toRole) {
        String[] parts = token.split("\\.");
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8).replace(fromRole, toRole);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return parts[0] + "." + encodedPayload + "." + parts[2];
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
