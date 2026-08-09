package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica actualización REST de ProductSpecification en OrderItem (SPR-033 Increment 11).
 */
@SpringBootTest
@Transactional
class OrderItemProductSpecificationUpdateApiContractTest {

    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("\"itemId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void updatesOrderItemProductSpecificationAndPersistsOnOrderDetail() throws Exception {
        PreparedOrder prepared = createOrderWithItem();

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/product-specification",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "garmentType": "Camiseta deportiva",
                                          "collarType": "Redondo",
                                          "sleeveType": "Corta",
                                          "garmentVariant": "Masculina",
                                          "sublimationRequired": true,
                                          "embroideryRequired": false,
                                          "dtfRequired": true,
                                          "decorationNotes": "Diseño frontal y trasero",
                                          "includesNames": true,
                                          "includesNumbers": true,
                                          "includesLogos": true,
                                          "personalizationNotes": "Nombre y número de cada jugador",
                                          "itemObservations": "Entregar muestra antes de producción"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(prepared.orderItemId().toString()))
                .andExpect(jsonPath("$.productSpecification.garmentType").value("Camiseta deportiva"))
                .andExpect(jsonPath("$.productSpecification.collarType").value("Redondo"))
                .andExpect(jsonPath("$.productSpecification.sleeveType").value("Corta"))
                .andExpect(jsonPath("$.productSpecification.garmentVariant").value("Masculina"))
                .andExpect(jsonPath("$.productSpecification.sublimationRequired").value(true))
                .andExpect(jsonPath("$.productSpecification.embroideryRequired").value(false))
                .andExpect(jsonPath("$.productSpecification.dtfRequired").value(true))
                .andExpect(jsonPath("$.productSpecification.includesNames").value(true))
                .andExpect(jsonPath("$.productSpecification.includesNumbers").value(true))
                .andExpect(jsonPath("$.productSpecification.includesLogos").value(true))
                .andExpect(jsonPath("$.productSpecification.decorationNotes")
                        .value("Diseño frontal y trasero"))
                .andExpect(jsonPath("$.productSpecification.personalizationNotes")
                        .value("Nombre y número de cada jugador"))
                .andExpect(jsonPath("$.productSpecification.itemObservations")
                        .value("Entregar muestra antes de producción"));

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType")
                        .value("Camiseta deportiva"))
                .andExpect(jsonPath("$.items[0].productSpecification.dtfRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.itemObservations")
                        .value("Entregar muestra antes de producción"))
                .andExpect(jsonPath("$.items[0].sizes").isArray());
    }

    @Test
    void rejectsProductSpecificationUpdateWhenItemDoesNotBelongToOrder() throws Exception {
        PreparedOrder orderA = createOrderWithItem();
        PreparedOrder orderB = createOrderWithItem();

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/product-specification",
                                orderA.orderId(), orderB.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "garmentType": "Camiseta",
                                          "collarType": "V",
                                          "sleeveType": "Larga",
                                          "garmentVariant": "Femenina",
                                          "sublimationRequired": false,
                                          "embroideryRequired": false,
                                          "dtfRequired": false,
                                          "decorationNotes": null,
                                          "includesNames": false,
                                          "includesNumbers": false,
                                          "includesLogos": false,
                                          "personalizationNotes": null,
                                          "itemObservations": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    private PreparedOrder createOrderWithItem() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Spec Update"));

        MvcResult quotationResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Spec Updater",
                                          "observations": "Update specification"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(14)))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID quotationId = extractUuid(quotationResult.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camiseta Deportiva",
                                          "quantity": 40,
                                          "fabric": "Hydrotech",
                                          "color": "Rojo",
                                          "unitPrice": 42000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        String orderNumber = "ORD-SPEC-UPD-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult createOrderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Spec Updater",
                                          "observations": "Spec update contract"
                                        }
                                        """.formatted(quotationId, orderNumber, LocalDate.now().plusDays(10)))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = extractUuid(createOrderResult.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        MvcResult orderResult = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andReturn();

        UUID orderItemId = extractUuid(orderResult.getResponse().getContentAsString(), ITEM_ID_PATTERN);
        return new PreparedOrder(orderId, orderItemId);
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }

    private record PreparedOrder(UUID orderId, UUID orderItemId) {
    }
}
