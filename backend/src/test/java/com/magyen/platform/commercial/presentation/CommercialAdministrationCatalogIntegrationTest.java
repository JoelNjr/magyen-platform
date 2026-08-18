package com.magyen.platform.commercial.presentation;

import com.magyen.platform.administration.application.dto.CreateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.dto.DeactivateAdministrationCatalogEntryCommand;
import com.magyen.platform.administration.application.usecase.CreateAdministrationCatalogEntryUseCase;
import com.magyen.platform.administration.application.usecase.DeactivateAdministrationCatalogEntryUseCase;
import com.magyen.platform.administration.domain.AdministrationCatalogKind;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CommercialAdministrationCatalogIntegrationTest {

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateAdministrationCatalogEntryUseCase createAdministrationCatalogEntryUseCase;

    @Autowired
    private DeactivateAdministrationCatalogEntryUseCase deactivateAdministrationCatalogEntryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void commercialCatalogsExposeActiveAdministrationValuesOnly() throws Exception {
        String inactiveName = "Prenda inactiva F " + UUID.randomUUID();
        UUID inactiveId = createAdministrationCatalogEntryUseCase.execute(
                new CreateAdministrationCatalogEntryCommand(AdministrationCatalogKind.GARMENT, inactiveName)
        ).catalogEntryId();
        deactivateAdministrationCatalogEntryUseCase.execute(new DeactivateAdministrationCatalogEntryCommand(inactiveId));

        mockMvc.perform(get("/api/v1/commercial-catalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Camiseta")))
                .andExpect(jsonPath("$.fabrics[*].label", hasItem("Piqué")))
                .andExpect(jsonPath("$.collarTypes[*].label", hasItem("Tejido")))
                .andExpect(jsonPath("$.sleeveTypes[*].label", hasItem("Manga larga sisa")))
                .andExpect(jsonPath("$.garmentTypes[*].label", not(hasItem(inactiveName))));
    }

    @Test
    void inactiveCatalogValuesCannotBeSelectedOnNewQuotationItems() throws Exception {
        String inactiveFabric = "Tela inactiva F " + UUID.randomUUID();
        UUID fabricId = createAdministrationCatalogEntryUseCase.execute(
                new CreateAdministrationCatalogEntryCommand(AdministrationCatalogKind.FABRIC, inactiveFabric)
        ).catalogEntryId();
        deactivateAdministrationCatalogEntryUseCase.execute(new DeactivateAdministrationCatalogEntryCommand(fabricId));

        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Producto con tela inactiva",
                                          "quantity": 1,
                                          "fabric": "%s",
                                          "color": "Blanco",
                                          "unitPrice": 10000
                                        }
                                        """.formatted(inactiveFabric))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void historicalInactiveCatalogValueRemainsReadable() throws Exception {
        String garmentName = "Prenda histórica F " + UUID.randomUUID();
        UUID garmentId = createAdministrationCatalogEntryUseCase.execute(
                new CreateAdministrationCatalogEntryCommand(AdministrationCatalogKind.GARMENT, garmentName)
        ).catalogEntryId();

        UUID quotationId = createDraftQuotation();
        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Producto histórico",
                                          "quantity": 2,
                                          "fabric": "Sudáfrica",
                                          "color": "Blanco",
                                          "unitPrice": 20000,
                                          "productSpecification": {
                                            "garmentType": "%s",
                                            "collarType": "Redondo",
                                            "sleeveType": "Manga corta sisa",
                                            "cuffRequired": false
                                          }
                                        }
                                        """.formatted(garmentName))
                )
                .andExpect(status().isCreated());

        deactivateAdministrationCatalogEntryUseCase.execute(new DeactivateAdministrationCatalogEntryCommand(garmentId));

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value(garmentName));
    }

    @Test
    void productSupportsOptionalSecondFabricAndOneFabricRemainsValid() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Conjunto deportivo",
                                          "quantity": 1,
                                          "fabric": "Sudáfrica",
                                          "secondaryFabric": "Piqué",
                                          "color": "Blanco",
                                          "unitPrice": 80000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta simple",
                                          "quantity": 1,
                                          "fabric": "Hydrotech",
                                          "color": "Blanco",
                                          "unitPrice": 25000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].fabric").value("Sudáfrica"))
                .andExpect(jsonPath("$.items[0].secondaryFabric").value("Piqué"))
                .andExpect(jsonPath("$.items[1].fabric").value("Hydrotech"))
                .andExpect(jsonPath("$.items[1].secondaryFabric").value(nullValue()));
    }

    private UUID createDraftQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente catálogo F " + UUID.randomUUID()));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor catálogo F " + UUID.randomUUID()
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(14), sellerId))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return extractUuid(result.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }
}
