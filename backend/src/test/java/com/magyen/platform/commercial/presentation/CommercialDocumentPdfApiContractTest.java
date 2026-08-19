package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CommercialDocumentPdfApiContractTest {

    private static final Pattern QUOTATION_ID_PATTERN =
            Pattern.compile("\"quotationId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("\"orderId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern ITEM_ID_PATTERN =
            Pattern.compile("\"itemId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");
    private static final Pattern QUOTATION_NUMBER_PATTERN =
            Pattern.compile("\"quotationNumber\"\\s*:\\s*(\\d+)");
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final UUID UNKNOWN_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

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
    void quotationPdfContainsBusinessContentWithoutTechnicalIdentifiers() throws Exception {
        PreparedQuotation quotation = createQuotationWithProduct();
        String expectedNumber = "C" + String.format("%06d", quotation.quotationNumber());

        MvcResult result = mockMvc.perform(get("/api/v1/quotations/{quotationId}/pdf", quotation.quotationId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString(expectedNumber)))
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));

        String text = extractText(pdf);
        assertTrue(text.contains("COTIZACIÓN"));
        assertTrue(text.contains(expectedNumber));
        assertTrue(text.contains("Sofia Vergara PDF"));
        assertTrue(text.contains("Carlos Ramirez PDF"));
        assertTrue(text.contains("Camisetas de voleibol PDF"));
        assertTrue(text.contains("Sudáfrica"));
        assertTrue(text.contains("Hydrotech"));
        assertTrue(text.contains("Blanco"));
        assertTrue(text.contains("Redondo"));
        assertTrue(text.contains("40.000") || text.contains("40000"));
        assertTrue(text.contains("480.000") || text.contains("480000"));
        assertFalse(UUID_PATTERN.matcher(text).find());
        assertFalse(text.contains(quotation.quotationId().toString()));
    }

    @Test
    void orderRemissionPdfContainsDeliveryPaymentAndReceiptFields() throws Exception {
        PreparedOrder order = createOrderWithSizes();

        MvcResult result = mockMvc.perform(get("/api/v1/orders/{orderId}/remission/pdf", order.orderId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("Remision-PDF-1")))
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));

        String text = extractText(pdf);
        assertTrue(text.contains("REMISIÓN"));
        assertTrue(text.contains("Documento de entrega"));
        assertFalse(text.contains("FACTURA"));
        assertTrue(text.contains("PDF-1"));
        assertTrue(text.contains("Sofia Vergara PDF"));
        assertTrue(text.contains("Camisetas de voleibol PDF"));
        assertTrue(text.contains("S: 3"));
        assertTrue(text.contains("M: 9"));
        assertTrue(text.contains("Fecha de entrega"));
        assertTrue(text.contains("Total pagado"));
        assertTrue(text.contains("Saldo pendiente"));
        assertTrue(text.contains("Recibido por"));
        assertTrue(text.contains("Firma"));
        assertFalse(UUID_PATTERN.matcher(text).find());
        assertFalse(text.contains(order.orderId().toString()));
    }

    @Test
    void unknownDocumentIdsFollowExistingNotFoundSemantics() throws Exception {
        mockMvc.perform(get("/api/v1/quotations/{quotationId}/pdf", UNKNOWN_ID))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/orders/{orderId}/remission/pdf", UNKNOWN_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedPdfRequestIsUnauthorized() throws Exception {
        MockMvc secured = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        secured.perform(get("/api/v1/quotations/{quotationId}/pdf", UNKNOWN_ID))
                .andExpect(status().isUnauthorized());
        secured.perform(get("/api/v1/orders/{orderId}/remission/pdf", UNKNOWN_ID))
                .andExpect(status().isUnauthorized());
    }

    private PreparedQuotation createQuotationWithProduct() throws Exception {
        Customer customer = customerRepository.save(Customer.create("Sofia Vergara PDF"));
        UUID sellerId = FixedSellerEmployeeFixture.create(createPayrollEmployeeUseCase, "Carlos Ramirez PDF");

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/quotations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "deliveryDate": "%s",
                                          "sellerId": "%s"
                                        }
                                        """.formatted(customer.getId(), LocalDate.of(2026, 9, 10), sellerId))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String createBody = createResult.getResponse().getContentAsString();
        UUID quotationId = extractUuid(createBody, QUOTATION_ID_PATTERN);
        long quotationNumber = extractLong(createBody, QUOTATION_NUMBER_PATTERN);

        mockMvc.perform(
                        post("/api/v1/quotations/{quotationId}/items", quotationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Camisetas de voleibol PDF",
                                          "quantity": 12,
                                          "fabric": "Sudáfrica",
                                          "secondaryFabric": "Hydrotech",
                                          "color": "Blanco",
                                          "unitPrice": 40000,
                                          "productSpecification": {
                                            "garmentType": "Camiseta",
                                            "collarType": "Redondo",
                                            "sleeveType": "Manga corta sisa",
                                            "cuffRequired": false,
                                            "itemObservations": "Uniforme de competencia"
                                          }
                                        }
                                        """)
                )
                .andExpect(status().isCreated());

        return new PreparedQuotation(quotationId, quotationNumber);
    }

    private PreparedOrder createOrderWithSizes() throws Exception {
        PreparedQuotation quotation = createQuotationWithProduct();
        mockMvc.perform(patch("/api/v1/quotations/{quotationId}/approve", quotation.quotationId()))
                .andExpect(status().isOk());

        MvcResult createOrder = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "quotationId": "%s",
                                          "orderNumber": "PDF-1",
                                          "description": "Pedido de remisión PDF",
                                          "deliveryDate": "2026-09-15"
                                        }
                                        """.formatted(quotation.quotationId()))
                )
                .andExpect(status().isCreated())
                .andReturn();
        UUID orderId = extractUuid(createOrder.getResponse().getContentAsString(), ORDER_ID_PATTERN);

        MvcResult orderDetail = mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andReturn();
        UUID itemId = extractUuid(orderDetail.getResponse().getContentAsString(), ITEM_ID_PATTERN);

        mockMvc.perform(
                        put("/api/v1/orders/{orderId}/items/{orderItemId}/sizes", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "sizes": [
                                            { "size": "S", "quantity": 3 },
                                            { "size": "M", "quantity": 9 }
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        return new PreparedOrder(orderId);
    }

    private String extractText(byte[] pdf) throws Exception {
        PdfReader reader = new PdfReader(pdf);
        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page)).append('\n');
            }
            return text.toString();
        } finally {
            reader.close();
        }
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

    private record PreparedQuotation(UUID quotationId, long quotationNumber) {
    }

    private record PreparedOrder(UUID orderId) {
    }
}
