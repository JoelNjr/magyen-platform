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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato REST de SizeBreakdown en OrderItem (SPR-033 Increment 9).
 */
@SpringBootTest
@Transactional
class OrderItemSizesApiContractTest {

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

    @Autowired
    private SellerRepository sellerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void replacesValidSizeBreakdownAndReturnsItOnOrderDetail() throws Exception {
        PreparedOrder prepared = createOrderWithItem(50, true);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 8 },
                                            { "size": "M", "quantity": 17 },
                                            { "size": "L", "quantity": 18 },
                                            { "size": "XL", "quantity": 7 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(prepared.orderItemId().toString()))
                .andExpect(jsonPath("$.sizes.length()").value(4))
                .andExpect(jsonPath("$.sizes[0].size").value("S"))
                .andExpect(jsonPath("$.sizes[0].quantity").value(8))
                .andExpect(jsonPath("$.sizes[1].size").value("M"))
                .andExpect(jsonPath("$.sizes[1].quantity").value(17))
                .andExpect(jsonPath("$.sizes[2].size").value("L"))
                .andExpect(jsonPath("$.sizes[2].quantity").value(18))
                .andExpect(jsonPath("$.sizes[3].size").value("XL"))
                .andExpect(jsonPath("$.sizes[3].quantity").value(7));

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(50))
                .andExpect(jsonPath("$.items[0].sizes.length()").value(4))
                .andExpect(jsonPath("$.items[0].sizes[0].size").value("S"))
                .andExpect(jsonPath("$.items[0].sizes[0].quantity").value(8))
                .andExpect(jsonPath("$.items[0].sizes[3].size").value("XL"))
                .andExpect(jsonPath("$.items[0].sizes[3].quantity").value(7))
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(true));
    }

    @Test
    void replacesOverlappingSizeLabelsWithoutUniqueConstraintCollision() throws Exception {
        PreparedOrder prepared = createOrderWithItem(50, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 10 },
                                            { "size": "M", "quantity": 17 },
                                            { "size": "L", "quantity": 18 },
                                            { "size": "XL", "quantity": 5 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes.length()").value(4));

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 8 },
                                            { "size": "M", "quantity": 12 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes.length()").value(2))
                .andExpect(jsonPath("$.sizes[0].size").value("S"))
                .andExpect(jsonPath("$.sizes[0].quantity").value(8))
                .andExpect(jsonPath("$.sizes[1].size").value("M"))
                .andExpect(jsonPath("$.sizes[1].quantity").value(12));

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].sizes.length()").value(2))
                .andExpect(jsonPath("$.items[0].sizes[0].quantity").value(8))
                .andExpect(jsonPath("$.items[0].sizes[1].quantity").value(12));
    }

    @Test
    void acceptsEmptySizesWithoutDeletingOrderItem() throws Exception {
        PreparedOrder prepared = createOrderWithItem(20, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "M", "quantity": 10 },
                                            { "size": "L", "quantity": 10 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes.length()").value(2));

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": []
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(prepared.orderItemId().toString()))
                .andExpect(jsonPath("$.sizes").isArray())
                .andExpect(jsonPath("$.sizes").isEmpty());

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].itemId").value(prepared.orderItemId().toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(20))
                .andExpect(jsonPath("$.items[0].sizes").isEmpty());
    }

    @Test
    void returnsEmptySizesForNewOrderItems() throws Exception {
        PreparedOrder prepared = createOrderWithItem(15, false);

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].sizes").isArray())
                .andExpect(jsonPath("$.items[0].sizes").isEmpty())
                .andExpect(jsonPath("$.items[0].productName").value("Camiseta Deportiva"))
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void rejectsDuplicateSizes() throws Exception {
        PreparedOrder prepared = createOrderWithItem(30, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "M", "quantity": 20 },
                                            { "size": "M", "quantity": 10 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsSizeTotalGreaterThanOrderItemQuantity() throws Exception {
        PreparedOrder prepared = createOrderWithItem(20, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 15 },
                                            { "size": "M", "quantity": 10 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsZeroAndNegativeSizeQuantity() throws Exception {
        PreparedOrder prepared = createOrderWithItem(20, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "L", "quantity": 0 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "L", "quantity": -2 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankSize() throws Exception {
        PreparedOrder prepared = createOrderWithItem(20, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "   ", "quantity": 5 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOrderItemThatDoesNotBelongToOrder() throws Exception {
        PreparedOrder orderA = createOrderWithItem(20, false);
        PreparedOrder orderB = createOrderWithItem(20, false);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                orderA.orderId(), orderB.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "M", "quantity": 10 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void preservesProductSpecificationWhenReplacingSizes() throws Exception {
        PreparedOrder prepared = createOrderWithItem(50, true);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes",
                                prepared.orderId(), prepared.orderItemId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 8 },
                                            { "size": "M", "quantity": 17 },
                                            { "size": "L", "quantity": 18 },
                                            { "size": "XL", "quantity": 7 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/orders/{orderId}", prepared.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productSpecification.garmentType").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].productSpecification.collarType").value("Redondo"))
                .andExpect(jsonPath("$.items[0].productSpecification.sleeveType").value("Manga corta sisa"))
                .andExpect(jsonPath("$.items[0].productSpecification.cuffRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.sublimationRequired").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNames").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesNumbers").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.includesLogos").value(true))
                .andExpect(jsonPath("$.items[0].productSpecification.itemObservations")
                        .value("Logo del patrocinador en pecho derecho"))
                .andExpect(jsonPath("$.items[0].sizes.length()").value(4));
    }

    private PreparedOrder createOrderWithItem(int quantity, boolean includeSpecification) throws Exception {
        UUID quotationId = createDraftQuotation();

        String itemPayload = includeSpecification
                ? """
                {
                  "productName": "Camiseta Deportiva",
                  "quantity": %d,
                  "fabric": "Sudáfrica",
                  "color": "Azul",
                  "unitPrice": 50000,
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
                """.formatted(quantity)
                : """
                {
                  "productName": "Camiseta Deportiva",
                  "quantity": %d,
                  "fabric": "Sudáfrica",
                  "color": "Azul",
                  "unitPrice": 50000
                }
                """.formatted(quantity);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(itemPayload)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        String orderNumber = "ORD-SIZE-API-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult createOrderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "deliveryDate": "%s",
                                          "observations": "Size contract"
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

    private UUID createDraftQuotation() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Size API"));
        Seller seller = sellerRepository.save(Seller.create("Size Tester " + UUID.randomUUID()));

        MvcResult result = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "observations": "SizeBreakdown contract"
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

    private record PreparedOrder(UUID orderId, UUID orderItemId) {
    }
}
