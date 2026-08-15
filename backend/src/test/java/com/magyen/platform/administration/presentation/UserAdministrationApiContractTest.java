package com.magyen.platform.administration.presentation;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UserAdministrationApiContractTest {

    private static final String PASSWORD = "correct-password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationUserRepository authenticationUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JsonMapper jsonMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void adminCanListUsers() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("list-admin"), AuthenticationRole.ADMIN));

        mockMvc.perform(authorized(get("/api/v1/admin/users"), accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.users[0].password").doesNotExist());
    }

    @Test
    void adminCanCreateOperatorAndAdmin() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("creator"), AuthenticationRole.ADMIN));
        String operatorUsername = uniqueUsername("new-operator");
        String adminUsername = uniqueUsername("new-admin");

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(operatorUsername, PASSWORD, "OPERATOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(operatorUsername))
                .andExpect(jsonPath("$.role").value("OPERATOR"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(adminUsername, PASSWORD, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void duplicateUsernameReturnsConflict() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("dup-admin"), AuthenticationRole.ADMIN));
        String username = uniqueUsername("duplicate");

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(username, PASSWORD, "OPERATOR")))
                .andExpect(status().isCreated());

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(username, PASSWORD, "OPERATOR")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void operatorCannotAdministerUsers() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("op"), AuthenticationRole.OPERATOR));

        mockMvc.perform(authorized(get("/api/v1/admin/users"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(uniqueUsername("blocked"), PASSWORD, "OPERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeactivateAndReactivateOperatorAndDisabledUserCannotLogin() throws Exception {
        AuthenticationUser admin = saveUser(uniqueUsername("lifecycle-admin"), AuthenticationRole.ADMIN);
        AuthenticationUser operator = saveUser(uniqueUsername("lifecycle-op"), AuthenticationRole.OPERATOR);
        String adminToken = loginAs(admin);
        String operatorToken = loginAs(operator);

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + operator.getId() + "/deactivate"), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(operator.getUsername(), PASSWORD))
                )
                .andExpect(status().isUnauthorized());

        mockMvc.perform(authorized(get("/api/v1/auth/me"), operatorToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + operator.getId() + "/activate"), adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(operator.getUsername(), PASSWORD))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OPERATOR"));
    }

    @Test
    void adminCanChangeRolesWhenAnotherActiveAdminExists() throws Exception {
        AuthenticationUser admin = saveUser(uniqueUsername("role-admin"), AuthenticationRole.ADMIN);
        AuthenticationUser operator = saveUser(uniqueUsername("role-op"), AuthenticationRole.OPERATOR);
        String adminToken = loginAs(admin);

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + operator.getId() + "/role"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "ADMIN" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + operator.getId() + "/role"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "OPERATOR" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OPERATOR"));
    }

    @Test
    void lastActiveAdminCannotBeDeactivatedOrDemoted() throws Exception {
        AuthenticationUser soleAdmin = saveUser(uniqueUsername("sole-admin"), AuthenticationRole.ADMIN);
        deactivateOtherEnabledAdministrators(soleAdmin);
        String accessToken = loginAs(soleAdmin);

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + soleAdmin.getId() + "/deactivate"), accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The last active administrator cannot be deactivated."));

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + soleAdmin.getId() + "/role"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "OPERATOR" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The last active administrator cannot be demoted."));

        mockMvc.perform(authorized(get("/api/v1/auth/me"), accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void invalidRoleIsRejected() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("invalid-role"), AuthenticationRole.ADMIN));

        mockMvc.perform(authorized(post("/api/v1/admin/users"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(uniqueUsername("client"), PASSWORD, "CLIENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changedRoleIsReconciledForExistingToken() throws Exception {
        AuthenticationUser admin = saveUser(uniqueUsername("reconcile-admin"), AuthenticationRole.ADMIN);
        AuthenticationUser operator = saveUser(uniqueUsername("reconcile-op"), AuthenticationRole.OPERATOR);
        String adminToken = loginAs(admin);
        String operatorToken = loginAs(operator);

        mockMvc.perform(authorized(get("/api/v1/admin/users"), operatorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(authorized(patch("/api/v1/admin/users/" + operator.getId() + "/role"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "role": "ADMIN" }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(authorized(get("/api/v1/admin/users"), operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].username", hasItem(operator.getUsername())));
    }

    @Test
    void listedUsersAreOrderedByUsername() throws Exception {
        String laterUsername = "z-" + uniqueUsername("later");
        String earlierUsername = "a-" + uniqueUsername("earlier");
        saveUser(laterUsername, AuthenticationRole.OPERATOR);
        saveUser(earlierUsername, AuthenticationRole.OPERATOR);
        String accessToken = loginAs(saveUser(uniqueUsername("order-admin"), AuthenticationRole.ADMIN));

        MvcResult result = mockMvc.perform(authorized(get("/api/v1/admin/users"), accessToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode users = jsonMapper.readTree(result.getResponse().getContentAsString()).get("users");
        int earlierIndex = indexOfUsername(users, earlierUsername);
        int laterIndex = indexOfUsername(users, laterUsername);
        org.junit.jupiter.api.Assertions.assertTrue(earlierIndex < laterIndex);
    }

    private void deactivateOtherEnabledAdministrators(AuthenticationUser soleAdmin) {
        authenticationUserRepository.findAllOrderByUsername().stream()
                .filter(user -> !user.getId().equals(soleAdmin.getId()))
                .filter(AuthenticationUser::isEnabledAdministrator)
                .forEach(user -> authenticationUserRepository.save(user.deactivate()));
    }

    private AuthenticationUser saveUser(String username, AuthenticationRole role) {
        return authenticationUserRepository.save(AuthenticationUser.create(
                username,
                passwordHasher.hash(PASSWORD),
                true,
                role
        ));
    }

    private String loginAs(AuthenticationUser authenticationUser) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson(authenticationUser.getUsername(), PASSWORD))
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

    private static String createUserJson(String username, String password, String role) {
        return """
                {
                  "username": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(username, password, role);
    }

    private static String loginJson(String username, String password) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }

    private static int indexOfUsername(JsonNode users, String username) {
        for (int index = 0; index < users.size(); index++) {
            if (username.equals(users.get(index).get("username").asText())) {
                return index;
            }
        }
        throw new AssertionError("Username not found: " + username);
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
