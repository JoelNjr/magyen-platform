package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.Seller;
import com.magyen.platform.commercial.domain.SellerRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato REST aditivo de ProductSpecification.
 */
@SpringBootTest
@Transactional
class ProductSpecificationApiContractTest {

    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SellerRepository sellerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void acceptsLegacyAddItemPayloadAndReturnsEmptyProductSpecification() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta Básica",
                                          "quantity": 10,
                                          "fabric": "Hydrotech",
                                          "color": "Negro",
                                          "unitPrice": 30000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Camiseta Básica"))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.embroideryRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.dtfRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNames").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNumbers").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.includesLogos").value(false));
    }

    @Test
    void persistsAndReturnsFullProductSpecificationThroughQuotationAndOrder() throws Exception {
        UUID quotationId = createDraftQuotation();

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta Deportiva",
                                          "quantity": 20,
                                          "fabric": "Hydrotech",
                                          "color": "Azul",
                                          "unitPrice": 45000,
                                          "productSpecification": {
                                            "garmentType": "Camiseta",
                                            "collarType": "Redondo",
                                            "sleeveType": "Manga corta sisa",
                                            "cuffRequired": true,
                                            "sublimationRequired": true,
                                            "embroideryRequired": false,
                                            "dtfRequired": false,
                                            "decorationNotes": "Sublimación total",
                                            "includesNames": true,
                                            "includesNumbers": true,
                                            "includesLogos": true,
                                            "personalizationNotes": "Nombre en espalda y número 11",
                                            "itemObservations": "Logo del patrocinador en pecho derecho"
                                          }
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.collarType").value("Redondo"))
                .andExpect(jsonPath("$.items[0].productSpecification.sleeveType").value("Manga corta sisa"))
                .andExpect(jsonPath("$.items[0].productSpecification.cuffRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.embroideryRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.dtfRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.decorationNotes").value("Sublimación total"))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNames").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNumbers").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesLogos").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.personalizationNotes")
                        .value("Nombre en espalda y número 11"))
                .andExpect(jsonPath("$.items[0].productSpecification.itemObservations")
                        .value("Logo del patrocinador en pecho derecho"));

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        String orderNumber = "ORD-SPEC-API-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult createOrderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "deliveryDate": "%s",
                                          "observations": "Spec contract"
                                        }
                                        """.formatted(quotationId, orderNumber, LocalDate.now().plusDays(10)))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = extractUuid(createOrderResult.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Camiseta Deportiva"))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNames").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNumbers").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesLogos").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.embroideryRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.itemObservations")
                        .value("Logo del patrocinador en pecho derecho"))
                .andExpect(jsonPath("$.items[0].sizes").isArray())
                .andExpect(jsonPath("$.items[0].sizes").isEmpty());
    }

    private UUID createDraftQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Spec API"));
        Seller seller = sellerRepository.save(Seller.create("API Tester " + UUID.randomUUID()));

        MvcResult result = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "observations": "ProductSpecification contract"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(14), seller.getId()))
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
