package com.magyen.platform.administration.presentation;

import com.magyen.platform.administration.application.port.PasswordHasher;
import com.magyen.platform.administration.domain.AuthenticationRole;
import com.magyen.platform.administration.domain.AuthenticationUser;
import com.magyen.platform.administration.domain.AuthenticationUserRepository;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdministrationCatalogApiContractTest {

    private static final String PASSWORD = "correct-password";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthenticationUserRepository authenticationUserRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void adminCanCreateListActivateAndDeactivateCatalogEntries() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("cat-admin"), AuthenticationRole.ADMIN));
        String garmentName = "Prenda F " + UUID.randomUUID();

        MvcResult created = mockMvc.perform(authorized(post("/api/v1/admin/catalogs/garments"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNameJson(garmentName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(garmentName))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.kind").value("GARMENT"))
                .andReturn();

        String catalogEntryId = jsonMapper.readTree(created.getResponse().getContentAsString())
                .get("catalogEntryId")
                .asText();

        mockMvc.perform(authorized(get("/api/v1/admin/catalogs"), accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garments[*].name", hasItem("Camiseta")))
                .andExpect(jsonPath("$.fabrics[*].name", hasItem("Sudáfrica")))
                .andExpect(jsonPath("$.collars[*].name", hasItem("Redondo")))
                .andExpect(jsonPath("$.sleeves[*].name", hasItem("Manga corta sisa")))
                .andExpect(jsonPath("$.garments[*].name", hasItem(garmentName)));

        mockMvc.perform(authorized(
                        patch("/api/v1/admin/catalogs/garments/{id}/deactivate", catalogEntryId),
                        accessToken
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(authorized(
                        patch("/api/v1/admin/catalogs/garments/{id}/activate", catalogEntryId),
                        accessToken
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void duplicateCatalogNameIsRejected() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("dup-cat"), AuthenticationRole.ADMIN));
        String name = "Cuello F " + UUID.randomUUID();

        mockMvc.perform(authorized(post("/api/v1/admin/catalogs/collars"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNameJson(name)))
                .andExpect(status().isCreated());

        mockMvc.perform(authorized(post("/api/v1/admin/catalogs/collars"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNameJson(name)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void operatorCannotManageCatalogs() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("cat-op"), AuthenticationRole.OPERATOR));

        mockMvc.perform(authorized(get("/api/v1/admin/catalogs"), accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(authorized(post("/api/v1/admin/catalogs/fabrics"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNameJson("Tela no autorizada")))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatingFabricCatalogDoesNotCreateInventoryOrFinance() throws Exception {
        String accessToken = loginAs(saveUser(uniqueUsername("cat-iso"), AuthenticationRole.ADMIN));
        int inventoryCount = inventoryItemRepository.findAll().size();
        int financeCount = financialTransactionRepository.findAllNewestFirst().size();
        String fabricName = "Tela catálogo F " + UUID.randomUUID();

        mockMvc.perform(authorized(post("/api/v1/admin/catalogs/fabrics"), accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNameJson(fabricName)))
                .andExpect(status().isCreated());

        assertEquals(inventoryCount, inventoryItemRepository.findAll().size());
        assertEquals(financeCount, financialTransactionRepository.findAllNewestFirst().size());
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
                                .content("""
                                        {
                                          "username": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(authenticationUser.getUsername(), PASSWORD))
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

    private static String createNameJson(String name) {
        return """
                {
                  "name": "%s"
                }
                """.formatted(name);
    }

    private static String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
