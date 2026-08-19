package com.magyen.platform.plotter.presentation;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PlotterJobApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    private MockMvc mockMvc;
    private CreateInventoryItemResult paperRoll;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        paperRoll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLTAPI-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter API",
                "PAPER",
                "METER",
                new BigDecimal("100.0000"),
                new BigDecimal("20.0000"),
                null,
                new BigDecimal("4500.00"),
                "PAPER",
                true
        ));
    }

    @Test
    void createsListsAndGetsPlotterJob() throws Exception {
        UUID customerId = UUID.randomUUID();

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 10.5,
                                          "pricePerMeter": 8000,
                                          "observations": "Trabajo para uniformes"
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plotterJobId").exists())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.paperInventoryItemId").value(paperRoll.inventoryItemId().toString()))
                .andExpect(jsonPath("$.printedMeters").value(10.5))
                .andExpect(jsonPath("$.pricePerMeter").value(8000.0))
                .andExpect(jsonPath("$.totalAmount").value(84000.0))
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.jobType").value("EXTERNAL"))
                .andExpect(jsonPath("$.observations").value("Trabajo para uniformes"))
                .andExpect(jsonPath("$.creationDate").exists())
                .andReturn();

        String plotterJobId = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.plotterJobId"
        );

        mockMvc.perform(get("/api/v1/plotter/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobs[*].plotterJobId", hasItem(plotterJobId)));

        mockMvc.perform(get("/api/v1/plotter/jobs/{plotterJobId}", plotterJobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plotterJobId").value(plotterJobId))
                .andExpect(jsonPath("$.totalAmount").value(84000.0))
                .andExpect(jsonPath("$.paidAmount").value(0.0))
                .andExpect(jsonPath("$.outstandingAmount").value(84000.0))
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        mockMvc.perform(
                        post("/api/v1/plotter/jobs/{plotterJobId}/payments", plotterJobId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "amount": 24000,
                                          "paymentDate": "2026-08-10",
                                          "observations": "Abono"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(24000.0))
                .andExpect(jsonPath("$.paidAmount").value(24000.0))
                .andExpect(jsonPath("$.outstandingAmount").value(60000.0));

        mockMvc.perform(get("/api/v1/plotter/jobs/{plotterJobId}/payments", plotterJobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1))
                .andExpect(jsonPath("$.paidAmount").value(24000.0))
                .andExpect(jsonPath("$.outstandingAmount").value(60000.0));

        mockMvc.perform(
                        post("/api/v1/plotter/jobs/{plotterJobId}/payments", plotterJobId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "amount": 100000,
                                          "paymentDate": "2026-08-11"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidMaterialInsufficientStockAndInvalidValues() throws Exception {
        CreateInventoryItemResult fabric = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "FABAPI-" + UUID.randomUUID().toString().substring(0, 8),
                "Tela",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null,
                null,
                null,
                "FABRIC",
                false
        ));

        UUID customerId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 1,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, fabric.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 200,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 0,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 10.5,
                                          "pricePerMeter": -1
                                        }
                                        """.formatted(customerId, paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "printedMeters": 10.5,
                                          "pricePerMeter": 8000
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsInternalMagyenJobWithOrderIdentityAndRejectsPayment() throws Exception {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-API-" + UUID.randomUUID().toString().substring(0, 8)
        );
        String customerName = "Sofia Vergara API";
        UUID customerId = createCustomerUseCase.execute(new CreateCustomerCommand(customerName)).customerId();

        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                LocalDate.of(2026, 8, 6),
                sellerId,
                null,
                LocalDate.of(2026, 7, 27)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camisetas de voleibol",
                10,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("40000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        var order = createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                "ORD-API-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                LocalDate.of(2026, 7, 29),
                LocalDate.of(2026, 8, 6),
                null
        ));

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "INTERNAL_MAGYEN",
                                          "orderId": "%s",
                                          "creationDate": "2026-08-03",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 6,
                                          "pricePerMeter": 8000,
                                          "observations": "Producción Magyen"
                                        }
                                        """.formatted(order.orderId(), paperRoll.inventoryItemId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobType").value("INTERNAL_MAGYEN"))
                .andExpect(jsonPath("$.orderId").value(order.orderId().toString()))
                .andExpect(jsonPath("$.orderNumber").value(order.orderNumber()))
                .andExpect(jsonPath("$.customerName").value(customerName))
                .andExpect(jsonPath("$.pricePerMeter").value(8000.0))
                .andExpect(jsonPath("$.totalAmount").value(48000.0))
                .andReturn();

        String plotterJobId = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.plotterJobId"
        );

        mockMvc.perform(get("/api/v1/plotter/jobs/{plotterJobId}", plotterJobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobType").value("INTERNAL_MAGYEN"))
                .andExpect(jsonPath("$.orderNumber").value(order.orderNumber()))
                .andExpect(jsonPath("$.customerName").value(customerName));

        mockMvc.perform(
                        post("/api/v1/plotter/jobs/{plotterJobId}/payments", plotterJobId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "amount": 1000,
                                          "paymentDate": "2026-08-04"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInternalJobWithoutOrderAndExternalJobWithOrder() throws Exception {
        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "INTERNAL_MAGYEN",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 6
                                        }
                                        """.formatted(paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/plotter/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "jobType": "EXTERNAL",
                                          "customerId": "%s",
                                          "orderId": "%s",
                                          "paperInventoryItemId": "%s",
                                          "printedMeters": 6,
                                          "pricePerMeter": 8000
                                        }
                                        """.formatted(UUID.randomUUID(), UUID.randomUUID(), paperRoll.inventoryItemId()))
                )
                .andExpect(status().isBadRequest());
    }
}
