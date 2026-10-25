package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.domain.Money;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class QuotationDeliveryDateAndItemDuplicationApiContractTest {

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("\"itemId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private static final LocalDate QUOTATION_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDate INITIAL_DELIVERY = LocalDate.of(2026, 8, 20);
    private static final LocalDate CONFIRMATION = LocalDate.of(2026, 8, 10);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void deliveryDatesStayIndependentAndADuplicatedItemCanBeEditedAlone() throws Exception {
        UUID quotationId = createQuotation();
        UUID sourceItemId = addItem(quotationId);

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/delivery-date", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-08-05\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryDate").value("2026-08-05"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.creationDate").value("2026-08-01"));

        mockMvc.perform(get("/api/v1/quotations")
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-08-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.quotations[?(@.quotationId == '%s')].deliveryDate".formatted(quotationId)
                ).value(contains("2026-08-05")));

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/discount", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"discountAmount\":10000}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        UUID orderId = createOrder(quotationId, INITIAL_DELIVERY);
        BigDecimal orderTotalBeforeDuplication = orderRepository.findById(orderId).orElseThrow().getTotal().getAmount();

        MvcResult duplicated = mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items/{itemId}/duplicate", quotationId, sourceItemId)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceItemId").value(sourceItemId.toString()))
                .andExpect(jsonPath("$.discountAmount").value(10000))
                .andReturn();
        UUID duplicateItemId = extractUuid(duplicated.getResponse().getContentAsString(), ITEM_ID_PATTERN);
        assertTrue(!duplicateItemId.equals(sourceItemId));

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].quantity".formatted(sourceItemId))
                        .value(contains(10)))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].fabric".formatted(duplicateItemId))
                        .value(contains("Hydrotech")))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].secondaryFabric".formatted(duplicateItemId))
                        .value(contains("Sudáfrica")))
                .andExpect(jsonPath(
                        "$.items[?(@.itemId == '%s')].productSpecification.garmentType".formatted(duplicateItemId)
                ).value(contains("Camiseta")))
                .andExpect(jsonPath("$.totalAmount").value(590000));

        mockMvc.perform(put("/api/v1/quotations/{quotationId}/items/{itemId}", quotationId, duplicateItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "Camiseta copia",
                                  "quantity": 2,
                                  "fabric": "Hydrotech",
                                  "secondaryFabric": "Sudáfrica",
                                  "color": "Azul",
                                  "unitPrice": 30000,
                                  "productSpecification": {
                                    "garmentType": "Camiseta",
                                    "collarType": "Redondo",
                                    "sleeveType": "Manga corta sisa",
                                    "cuffRequired": true,
                                    "sublimationRequired": true,
                                    "embroideryRequired": false,
                                    "dtfRequired": false,
                                    "decorationNotes": "Frente",
                                    "includesNames": true,
                                    "includesNumbers": false,
                                    "includesLogos": true,
                                    "personalizationNotes": "Espalda",
                                    "itemObservations": "Logo pequeño"
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].quantity".formatted(sourceItemId))
                        .value(contains(10)))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].productName".formatted(sourceItemId))
                        .value(contains("Camiseta Deportiva")))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].quantity".formatted(duplicateItemId))
                        .value(contains(2)))
                .andExpect(jsonPath("$.items[?(@.itemId == '%s')].productName".formatted(duplicateItemId))
                        .value(contains("Camiseta copia")));

        Order orderAfterItemEdit = orderRepository.findById(orderId).orElseThrow();
        assertEquals(1, orderAfterItemEdit.getItems().size());
        assertEquals(orderTotalBeforeDuplication, orderAfterItemEdit.getTotal().getAmount());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/delivery-date", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-09-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryDate").value("2026-09-01"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryCommitment.promisedDeliveryDate").value("2026-08-20"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/promised-delivery-date", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promisedDeliveryDate\":\"2026-08-12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promisedDeliveryDate").value("2026-08-12"))
                .andExpect(jsonPath("$.confirmationDate").value("2026-08-10"))
                .andExpect(jsonPath("$.actualDeliveryDate").isEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertEquals(LocalDate.of(2026, 8, 12), persisted.getDeliveryCommitment().getPromisedDeliveryDate());
        assertNull(persisted.getActualDeliveryDate());
        assertEquals(OrderStatus.CONFIRMED, persisted.getStatus());
        assertEquals("No tocar", persisted.getObservations());

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryDate").value("2026-09-01"));

        mockMvc.perform(get("/api/v1/orders")
                        .param("fromDate", "2026-08-10")
                        .param("toDate", "2026-08-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.orders[?(@.orderId == '%s')].promisedDeliveryDate".formatted(orderId)
                ).value(contains("2026-08-12")));
    }

    @Test
    void invalidDeliveryDatesAreRejectedAndFrozenOrdersStayUnchanged() throws Exception {
        UUID quotationId = createQuotation();

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/delivery-date", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/delivery-date", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/delivery-date", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryDate\":\"2026-07-31\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryDate").value("2026-08-20"));

        Order confirmed = saveOrder(OrderStatus.CONFIRMED, null);
        mockMvc.perform(patch("/api/v1/orders/{orderId}/promised-delivery-date", confirmed.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/orders/{orderId}/promised-delivery-date", confirmed.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promisedDeliveryDate\":\"2026-08-01\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(
                CONFIRMATION.plusDays(20),
                orderRepository.findById(confirmed.getId()).orElseThrow()
                        .getDeliveryCommitment().getPromisedDeliveryDate()
        );

        Order delivered = saveOrder(OrderStatus.DELIVERED, LocalDate.of(2026, 9, 1));
        mockMvc.perform(patch("/api/v1/orders/{orderId}/promised-delivery-date", delivered.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promisedDeliveryDate\":\"2026-09-15\"}"))
                .andExpect(status().isBadRequest());
        Order deliveredAfter = orderRepository.findById(delivered.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, deliveredAfter.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), deliveredAfter.getActualDeliveryDate());
        assertEquals(
                CONFIRMATION.plusDays(20),
                deliveredAfter.getDeliveryCommitment().getPromisedDeliveryDate()
        );

        Order closed = saveOrder(OrderStatus.CLOSED, LocalDate.of(2026, 9, 2));
        mockMvc.perform(patch("/api/v1/orders/{orderId}/promised-delivery-date", closed.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promisedDeliveryDate\":\"2026-09-20\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(OrderStatus.CLOSED, orderRepository.findById(closed.getId()).orElseThrow().getStatus());
    }

    private UUID createQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente fechas " + UUID.randomUUID()));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Fechas " + UUID.randomUUID()
        );
        MvcResult result = mockMvc.perform(post("/api/v1/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "quotationDate": "%s",
                                  "deliveryDate": "%s",
                                  "sellerId": "%s",
                                  "observations": "Fecha y duplicado"
                                }
                                """.formatted(customer.getId(), QUOTATION_DATE, INITIAL_DELIVERY, sellerId)))
                .andExpect(status().isCreated())
                .andReturn();
        return extractUuid(result.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);
    }

    private UUID addItem(UUID quotationId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/quotations/{quotationId}/items", quotationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "Camiseta Deportiva",
                                  "quantity": 10,
                                  "fabric": "Hydrotech",
                                  "secondaryFabric": "Sudáfrica",
                                  "color": "Blanco",
                                  "unitPrice": 30000,
                                  "productSpecification": {
                                    "garmentType": "Camiseta",
                                    "collarType": "Redondo",
                                    "sleeveType": "Manga corta sisa",
                                    "cuffRequired": true,
                                    "sublimationRequired": true,
                                    "embroideryRequired": false,
                                    "dtfRequired": false,
                                    "decorationNotes": "Frente",
                                    "includesNames": true,
                                    "includesNumbers": false,
                                    "includesLogos": true,
                                    "personalizationNotes": "Espalda",
                                    "itemObservations": "Logo pequeño"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return extractUuid(result.getResponse().getContentAsString(), ITEM_ID_PATTERN);
    }

    private UUID createOrder(UUID quotationId, LocalDate deliveryDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quotationId": "%s",
                                  "confirmationDate": "%s",
                                  "deliveryDate": "%s",
                                  "observations": "No tocar"
                                }
                                """.formatted(quotationId, CONFIRMATION, deliveryDate)))
                .andExpect(status().isCreated())
                .andReturn();
        return extractUuid(result.getResponse().getContentAsString(), ORDER_ID_PATTERN);
    }

    private Order saveOrder(OrderStatus status, LocalDate actualDeliveryDate) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta ciclo",
                1,
                "Hydrotech",
                "Blanco",
                Money.of(new BigDecimal("120000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Order created = Order.create(
                OrderNumber.of("ORD-DATE-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                CONFIRMATION,
                DeliveryCommitment.of(CONFIRMATION.plusDays(20)),
                UUID.randomUUID(),
                "fecha api",
                List.of(item)
        );
        if (status == OrderStatus.CONFIRMED) {
            return orderRepository.save(created);
        }
        return orderRepository.save(Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                status,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                actualDeliveryDate
        ));
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }
}
