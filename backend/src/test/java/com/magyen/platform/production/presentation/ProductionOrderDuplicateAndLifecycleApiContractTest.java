package com.magyen.platform.production.presentation;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.production.domain.exception.ProductionOrderAlreadyExistsException;
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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica semántica HTTP de duplicados, snapshot de producción y robustez del lifecycle E2E.
 */
@SpringBootTest
@Transactional
class ProductionOrderDuplicateAndLifecycleApiContractTest {

    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern PRODUCTION_ORDER_ID_PATTERN =
            Pattern.compile("\"productionOrderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("\"itemId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern OPERATION_ID_PATTERN =
            Pattern.compile("\"operationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern PRODUCTION_ITEM_ID_PATTERN =
            Pattern.compile("\"productionItemId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

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
    void createsProductionOrderWithSnapshotAndRejectsDuplicateWithConflict() throws Exception {
        ConfirmedOrderFixture fixture = createConfirmedOrderWithSpecificationAndSizes();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProductionPayload(fixture.orderId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productionOrderId").exists())
                .andExpect(jsonPath("$.orderId").value(fixture.orderId().toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();

        UUID productionOrderId = extractUuid(
                createResult.getResponse().getContentAsString(),
                PRODUCTION_ORDER_ID_PATTERN
        );

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productionOrderId").value(productionOrderId.toString()))
                .andExpect(jsonPath("$.orderId").value(fixture.orderId().toString()))
                .andExpect(jsonPath("$.orderNumber").value(fixture.orderNumber()))
                .andExpect(jsonPath("$.customerName").value(fixture.customerName()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Camiseta Deportiva"))
                .andExpect(jsonPath("$.items[0].quantity").value(20))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.collarType").value("Redondo"))
                .andExpect(jsonPath("$.items[0].productSpecification.sleeveType").value("Corta"))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentVariant").value("Dry-fit"))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.embroideryRequired").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.dtfRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.decorationNotes").value("Full print"))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNames").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNumbers").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesLogos").value(false))
                .andExpect(jsonPath("$.items[0].productSpecification.personalizationNotes")
                        .value("Roster completo"))
                .andExpect(jsonPath("$.items[0].productSpecification.itemObservations")
                        .value("Prioridad alta"))
                .andExpect(jsonPath("$.items[0].sizes", hasSize(3)))
                .andExpect(jsonPath("$.items[0].sizes[0].size").value("S"))
                .andExpect(jsonPath("$.items[0].sizes[0].quantity").value(3))
                .andExpect(jsonPath("$.items[0].sizes[1].size").value("M"))
                .andExpect(jsonPath("$.items[0].sizes[1].quantity").value(7))
                .andExpect(jsonPath("$.items[0].sizes[2].size").value("L"))
                .andExpect(jsonPath("$.items[0].sizes[2].quantity").value(10));

        mockMvc.perform(get("/api/v1/production-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.productionOrders[?(@.productionOrderId=='" + productionOrderId + "')].orderNumber"
                ).value(hasItem(fixture.orderNumber())))
                .andExpect(jsonPath(
                        "$.productionOrders[?(@.productionOrderId=='" + productionOrderId + "')].customerName"
                ).value(hasItem(fixture.customerName())))
                .andExpect(jsonPath(
                        "$.productionOrders[?(@.productionOrderId=='" + productionOrderId + "')].productionOrderId"
                ).value(hasItem(productionOrderId.toString())));

        mockMvc.perform(
                        post("/api/v1/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProductionPayload(fixture.orderId()))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value(ProductionOrderAlreadyExistsException.DEFAULT_MESSAGE));

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productionOrderId").value(productionOrderId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void otherProductionDomainExceptionsRemainBadRequest() throws Exception {
        ConfirmedOrderFixture fixture = createConfirmedOrderWithSpecificationAndSizes();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProductionPayload(fixture.orderId()))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID productionOrderId = extractUuid(
                createResult.getResponse().getContentAsString(),
                PRODUCTION_ORDER_ID_PATTERN
        );

        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(1);

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/plan", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "plannedStartDate": "%s",
                                          "plannedEndDate": "%s",
                                          "priority": "HIGH"
                                        }
                                        """.formatted(start, end))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void productionSnapshotRemainsIndependentAndLifecycleCompletesEndToEnd() throws Exception {
        ConfirmedOrderFixture fixture = createConfirmedOrderWithSpecificationAndSizes();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/production-orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createProductionPayload(fixture.orderId()))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID productionOrderId = extractUuid(
                createResult.getResponse().getContentAsString(),
                PRODUCTION_ORDER_ID_PATTERN
        );

        MvcResult detailAfterCreate = mockMvc.perform(
                        get("/api/v1/production-orders/{productionOrderId}", productionOrderId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andReturn();

        UUID productionItemId = extractUuid(
                detailAfterCreate.getResponse().getContentAsString(),
                PRODUCTION_ITEM_ID_PATTERN
        );

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{itemId}/product-specification",
                                fixture.orderId(), fixture.itemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "garmentType": "Polo",
                                          "collarType": "Mao",
                                          "sleeveType": "Larga",
                                          "garmentVariant": "Premium",
                                          "sublimationRequired": false,
                                          "embroideryRequired": true,
                                          "dtfRequired": false,
                                          "decorationNotes": "Changed after snapshot",
                                          "includesNames": false,
                                          "includesNumbers": false,
                                          "includesLogos": true,
                                          "personalizationNotes": "New notes",
                                          "itemObservations": "Changed"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productionItemId").value(productionItemId.toString()))
                .andExpect(jsonPath("$.items[0].productName").value("Camiseta Deportiva"))
                .andExpect(jsonPath("$.items[0].quantity").value(20))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.decorationNotes").value("Full print"))
                .andExpect(jsonPath("$.items[0].sizes", hasSize(3)))
                .andExpect(jsonPath("$.items[0].sizes[0].size").value("S"))
                .andExpect(jsonPath("$.items[0].sizes[0].quantity").value(3));

        LocalDate plannedStart = LocalDate.now().plusDays(1);
        LocalDate plannedEnd = LocalDate.now().plusDays(8);

        MvcResult addOperationResult = mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/operations", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "CUTTING",
                                          "plannedStartDate": "%s",
                                          "plannedEndDate": "%s",
                                          "observations": "Corte"
                                        }
                                        """.formatted(plannedStart, plannedEnd))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID cuttingOperationId = extractUuid(
                addOperationResult.getResponse().getContentAsString(),
                OPERATION_ID_PATTERN
        );

        MvcResult sewingResult = mockMvc.perform(
                        post("/api/v1/production-orders/{productionOrderId}/operations", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "SEWING",
                                          "plannedStartDate": "%s",
                                          "plannedEndDate": "%s",
                                          "observations": "Costura"
                                        }
                                        """.formatted(plannedStart.plusDays(1), plannedEnd))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID sewingOperationId = extractUuid(
                sewingResult.getResponse().getContentAsString(),
                OPERATION_ID_PATTERN
        );

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/assign-operator",
                                productionOrderId, cuttingOperationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "assignedOperator": "Operario A" }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/assign-operator",
                                productionOrderId, sewingOperationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "assignedOperator": "Operario B" }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/start",
                                productionOrderId, cuttingOperationId)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/plan", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "plannedStartDate": "%s",
                                          "plannedEndDate": "%s",
                                          "priority": "HIGH"
                                        }
                                        """.formatted(plannedStart, plannedEnd))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLANNED"));

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/plan", productionOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "plannedStartDate": "%s",
                                          "plannedEndDate": "%s",
                                          "priority": "URGENT"
                                        }
                                        """.formatted(plannedStart, plannedEnd))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/production-orders/{productionOrderId}/start", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/start",
                                productionOrderId, cuttingOperationId)
                )
                .andExpect(status().isOk());
        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/complete",
                                productionOrderId, cuttingOperationId)
                )
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/production-orders/{productionOrderId}/complete", productionOrderId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/start",
                                productionOrderId, sewingOperationId)
                )
                .andExpect(status().isOk());
        mockMvc.perform(
                        patch("/api/v1/production-orders/{productionOrderId}/operations/{operationId}/complete",
                                productionOrderId, sewingOperationId)
                )
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/production-orders/{productionOrderId}/complete", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/production-orders/{productionOrderId}", productionOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.operations", hasSize(2)))
                .andExpect(jsonPath("$.operations[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.operations[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productionItemId").value(productionItemId.toString()))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].sizes", hasSize(3)));
    }

    private ConfirmedOrderFixture createConfirmedOrderWithSpecificationAndSizes() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Prod E2E"));

        MvcResult quotationResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Production Guard",
                                          "observations": "E2E production source"
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
                                          "quantity": 20,
                                          "fabric": "Hydrotech",
                                          "color": "Azul",
                                          "unitPrice": 45000
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        String orderNumber = "ORD-E2E-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult orderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Production Guard",
                                          "observations": "Confirmed source order"
                                        }
                                        """.formatted(quotationId, orderNumber, LocalDate.now().plusDays(10)))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = extractUuid(orderResult.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        MvcResult orderDetail = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andReturn();

        UUID itemId = extractUuid(orderDetail.getResponse().getContentAsString(), ITEM_ID_PATTERN);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{itemId}/product-specification", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "garmentType": "Camiseta",
                                          "collarType": "Redondo",
                                          "sleeveType": "Corta",
                                          "garmentVariant": "Dry-fit",
                                          "sublimationRequired": true,
                                          "embroideryRequired": false,
                                          "dtfRequired": true,
                                          "decorationNotes": "Full print",
                                          "includesNames": true,
                                          "includesNumbers": true,
                                          "includesLogos": false,
                                          "personalizationNotes": "Roster completo",
                                          "itemObservations": "Prioridad alta"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{itemId}/sizes", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 3 },
                                            { "size": "M", "quantity": 7 },
                                            { "size": "L", "quantity": 10 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        return new ConfirmedOrderFixture(orderId, itemId, orderNumber, customer.getName());
    }

    private String createProductionPayload(UUID orderId) {
        return """
                {
                  "orderId": "%s",
                  "priority": "NORMAL"
                }
                """.formatted(orderId);
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }

    private record ConfirmedOrderFixture(
            UUID orderId,
            UUID itemId,
            String orderNumber,
            String customerName
    ) {
    }
}
