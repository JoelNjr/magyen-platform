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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica la regla de negocio: una Cotización puede crear como máximo una Orden.
 */
@SpringBootTest
@Transactional
class OneOrderPerQuotationApiContractTest {

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
    void rejectsSecondOrderForSameQuotationAndExposesOrderIdOnQuotationDetail() throws Exception {
        UUID quotationId = createApprovedQuotationWithItem();

        String firstOrderNumber = "ORD-ONE-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult firstCreate = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createOrderPayload(quotationId, firstOrderNumber))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = extractUuid(firstCreate.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        String secondOrderNumber = "ORD-DUP-" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createOrderPayload(quotationId, secondOrderNumber))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una orden para esta cotización."));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.quotationId").value(quotationId.toString()))
                .andExpect(jsonPath("$.orderNumber").value(firstOrderNumber));
    }

    @Test
    void rejectsOrderCreationFromDraftQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Draft Order"));
        Seller seller = sellerRepository.save(Seller.create("Draft Guard " + UUID.randomUUID()));

        MvcResult quotationResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "observations": "Draft cannot create order"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(12), seller.getId()))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID quotationId = extractUuid(quotationResult.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Polo",
                                          "quantity": 5,
                                          "fabric": "Piqué",
                                          "color": "Blanco",
                                          "unitPrice": 25000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createOrderPayload(
                                        quotationId,
                                        "ORD-DRAFT-" + UUID.randomUUID().toString().substring(0, 8)
                                ))
                )
                .andExpect(status().isBadRequest());
    }

    private UUID createApprovedQuotationWithItem() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente One Order"));
        Seller seller = sellerRepository.save(Seller.create("One Order Guard " + UUID.randomUUID()));

        MvcResult quotationResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "observations": "One order per quotation"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(14), seller.getId()))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID quotationId = extractUuid(quotationResult.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta",
                                          "quantity": 12,
                                          "fabric": "Sudáfrica",
                                          "color": "Negro",
                                          "unitPrice": 30000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(nullValue()));

        return quotationId;
    }

    private String createOrderPayload(UUID quotationId, String orderNumber) {
        return """
                {
                  "quotationId": "%s",
                  "orderNumber": "%s",
                  "deliveryDate": "%s",
                  "observations": "Duplicate prevention"
                }
                """.formatted(quotationId, orderNumber, LocalDate.now().plusDays(10));
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }
}
