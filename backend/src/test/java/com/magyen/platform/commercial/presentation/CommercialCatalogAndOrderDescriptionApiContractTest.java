package com.magyen.platform.commercial.presentation;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato SPR-038 Increment A: catálogos comerciales, descripción de pedido y origen de cotización.
 */
@SpringBootTest
@Transactional
class CommercialCatalogAndOrderDescriptionApiContractTest {

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern QUOTATION_NUMBER_PATTERN =
            Pattern.compile("\"quotationNumber\"\\s*:\\s*(\\d+)");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void catalogsEndpointReturnsClosedCommercialValuesWithoutDuplicatingLabels() throws Exception {
        mockMvc.perform(get("/api/v1/commercial-catalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Camiseta")))
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Camiseta tipo polo")))
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Conjunto deportivo")))
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Conjunto de presentación")))
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Pantaloneta")))
                .andExpect(jsonPath("$.garmentTypes[*].label", hasItem("Otro")))
                .andExpect(jsonPath("$.garmentTypes[*].label", not(hasItem("Camiseta deportiva"))))
                .andExpect(jsonPath("$.collarTypes[*].label", hasItem("Redondo")))
                .andExpect(jsonPath("$.collarTypes[*].label", hasItem("En V recto")))
                .andExpect(jsonPath("$.collarTypes[*].label", hasItem("En V cruzado")))
                .andExpect(jsonPath("$.collarTypes[*].label", hasItem("Tejido")))
                .andExpect(jsonPath("$.sleeveTypes[*].label", hasItem("Manga corta sisa")))
                .andExpect(jsonPath("$.sleeveTypes[*].label", hasItem("Manga corta rangla")))
                .andExpect(jsonPath("$.sleeveTypes[*].label", hasItem("Manga larga sisa")))
                .andExpect(jsonPath("$.sleeveTypes[*].label", hasItem("Manga larga rangla")))
                .andExpect(jsonPath("$.cuffOptions[0].label").value("Sí"))
                .andExpect(jsonPath("$.cuffOptions[0].value").value(true))
                .andExpect(jsonPath("$.cuffOptions[1].label").value("No"))
                .andExpect(jsonPath("$.cuffOptions[1].value").value(false))
                .andExpect(jsonPath("$.fabrics[*].label", hasItem("Sudáfrica")))
                .andExpect(jsonPath("$.fabrics[*].label", hasItem("Hydrotech")))
                .andExpect(jsonPath("$.fabrics[*].label", hasItem("Piqué")));
    }

    @Test
    void rejectsUnknownGarmentCollarSleeveAndFreeTextFabric() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Prenda inválida",
                                          "quantity": 1,
                                          "fabric": "Tela inventada",
                                          "color": "Blanco",
                                          "unitPrice": 10000
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Prenda inválida",
                                          "quantity": 1,
                                          "fabric": "Sudáfrica",
                                          "color": "Blanco",
                                          "unitPrice": 10000,
                                          "productSpecification": {
                                            "garmentType": "Camiseta deportiva",
                                            "collarType": "Redondo",
                                            "sleeveType": "Manga corta sisa",
                                            "cuffRequired": true
                                          }
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsSudafricaFabricWhenInventoryHasNoStock() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta de prueba",
                                          "quantity": 10,
                                          "fabric": "Sudáfrica",
                                          "color": "Blanco",
                                          "unitPrice": 25000,
                                          "productSpecification": {
                                            "garmentType": "Camiseta",
                                            "collarType": "Redondo",
                                            "sleeveType": "Manga corta sisa",
                                            "cuffRequired": false
                                          }
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].fabric").value("Sudáfrica"))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.collarType").value("Redondo"))
                .andExpect(jsonPath("$.items[0].productSpecification.sleeveType").value("Manga corta sisa"))
                .andExpect(jsonPath("$.items[0].productSpecification.cuffRequired").value(false));
    }

    @Test
    void orderDescriptionPersistsAndQuotationOriginReturnsBusinessNumberNotUuid() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camisetas de voleibol",
                                          "quantity": 12,
                                          "fabric": "Hydrotech",
                                          "color": "Blanco",
                                          "unitPrice": 40000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        MvcResult quotationDetail = mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andReturn();
        long quotationNumber = extractLong(
                quotationDetail.getResponse().getContentAsString(),
                QUOTATION_NUMBER_PATTERN
        );
        String expectedDisplay = "C" + String.format("%06d", quotationNumber);
        String orderNumber = "1";

        MvcResult createOrderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "description": "Camisetas de voleibol",
                                          "deliveryDate": "%s"
                                        }
                                        """.formatted(quotationId, orderNumber, LocalDate.now().plusDays(10)))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andReturn();

        UUID orderId = extractUuid(createOrderResult.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.description").value("Camisetas de voleibol"))
                .andExpect(jsonPath("$.quotationId").value(quotationId.toString()))
                .andExpect(jsonPath("$.quotationNumber").value((int) quotationNumber))
                .andExpect(jsonPath("$.quotationNumberDisplay").value(expectedDisplay));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[?(@.orderId=='" + orderId + "')].orderNumber")
                        .value(hasItem(orderNumber)))
                .andExpect(jsonPath("$.orders[?(@.orderId=='" + orderId + "')].description")
                        .value(hasItem("Camisetas de voleibol")))
                .andExpect(jsonPath("$.orders[?(@.orderId=='" + orderId + "')].quotationNumberDisplay")
                        .value(hasItem(expectedDisplay)));

        assertTrue(expectedDisplay.startsWith("C"));
        assertEquals(orderNumber, "1");
    }

    private UUID createDraftQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente catálogo " + UUID.randomUUID()));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor catálogo " + UUID.randomUUID()
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

    private long extractLong(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "Number not found in response: " + body);
        return Long.parseLong(matcher.group(1));
    }
}
