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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica fecha histórica, vendedor controlado, color de base y total calculado.
 */
@SpringBootTest
@Transactional
class HistoricalCommercialDataApiContractTest {

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

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
    void listsEligibleFixedEmployeesAsSellersAndDoesNotCreateIndependentCatalog() throws Exception {
        UUID sellerEmployeeId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Historical Seller " + UUID.randomUUID()
        );

        mockMvc.perform(get("/api/v1/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellers[?(@.sellerId == '%s')]".formatted(sellerEmployeeId)).exists());

        assertFalse(webApplicationContext.containsBean("createSellerUseCase"));
    }

    @Test
    void persistsExplicitQuotationDateSellerIdAndIgnoresFreeTextSeller() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Histórico"));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor Controlado " + UUID.randomUUID()
        );
        LocalDate historicalDate = LocalDate.of(2026, 7, 27);
        LocalDate deliveryDate = LocalDate.of(2026, 8, 6);

        MvcResult created = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "quotationDate": "%s",
                                          "salesperson": "Texto Libre Inventado",
                                          "observations": "Fecha histórica"
                                        }
                                        """.formatted(
                                        customer.getId(),
                                        deliveryDate,
                                        sellerId,
                                        historicalDate
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creationDate").value(historicalDate.toString()))
                .andReturn();

        UUID quotationId = extractUuid(created.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creationDate").value(historicalDate.toString()))
                .andExpect(jsonPath("$.deliveryDate").value(deliveryDate.toString()))
                .andExpect(jsonPath("$.sellerId").value(sellerId.toString()))
                .andExpect(jsonPath("$.sellerName").value(org.hamcrest.Matchers.startsWith("Vendedor Controlado")))
                .andExpect(jsonPath("$.salesperson").doesNotExist());
    }

    @Test
    void defaultsQuotationDateToTodayWhenOmitted() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Fecha Default"));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor Default " + UUID.randomUUID()
        );

        mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s"
                                        }
                                        """.formatted(
                                        customer.getId(),
                                        LocalDate.now().plusDays(10),
                                        sellerId
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creationDate").value(LocalDate.now().toString()));
    }

    @Test
    void rejectsUnknownSellerAndFreeTextOnlyCreate() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Sin Vendedor"));

        mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s"
                                        }
                                        """.formatted(
                                        customer.getId(),
                                        LocalDate.now().plusDays(5),
                                        UUID.randomUUID()
                                ))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Solo Texto"
                                        }
                                        """.formatted(customer.getId(), LocalDate.now().plusDays(5)))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculatesLineTotalAndAcceptsBlancoBaseColorThenCopiesSellerToOrder() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Cliente Sublimado"));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor Orden"
        );
        LocalDate quotationDate = LocalDate.of(2026, 7, 27);
        LocalDate deliveryDate = LocalDate.of(2026, 8, 6);

        MvcResult quotationResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s",
                                          "quotationDate": "%s"
                                        }
                                        """.formatted(
                                        customer.getId(),
                                        deliveryDate,
                                        sellerId,
                                        quotationDate
                                ))
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID quotationId = extractUuid(quotationResult.getResponse().getContentAsString(), QUOTATION_ID_PATTERN);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Volleyball shirt",
                                          "quantity": 10,
                                          "fabric": "Sudáfrica",
                                          "color": "Blanco",
                                          "unitPrice": 40000
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(400000));

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotationNumber").isNumber())
                .andExpect(jsonPath("$.items[0].color").value("Blanco"))
                .andExpect(jsonPath("$.items[0].quantity").value(10))
                .andExpect(jsonPath("$.items[0].unitPrice").value(40000))
                .andExpect(jsonPath("$.items[0].subtotal").value(400000))
                .andExpect(jsonPath("$.totalAmount").value(400000));

        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotationId))
                .andExpect(status().isOk());

        String orderNumber = "1";
        LocalDate orderDeliveryDate = LocalDate.now().plusDays(10);
        MvcResult orderResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "%s",
                                          "deliveryDate": "%s",
                                          "salesperson": "Otro Nombre",
                                          "observations": "Pedido histórico"
                                        }
                                        """.formatted(quotationId, orderNumber, orderDeliveryDate))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("1"))
                .andReturn();

        UUID orderId = extractUuid(orderResult.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("1"))
                .andExpect(jsonPath("$.sellerId").value(sellerId.toString()))
                .andExpect(jsonPath("$.sellerName").value("Vendedor Orden"))
                .andExpect(jsonPath("$.salesperson").doesNotExist())
                .andExpect(jsonPath("$.items[0].color").value("Blanco"))
                .andExpect(jsonPath("$.totalAmount").value(400000));

        mockMvc.perform(get("/api/v1/quotations/{quotationId}", quotationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderId").value(org.hamcrest.Matchers.not(nullValue())));
    }

    private UUID extractUuid(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "UUID not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }
}
