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
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PlotterProfitabilityApiContractTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2099, 4, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2099, 4, 30);
    private static final LocalDate JOB_DATE = LocalDate.of(2099, 4, 8);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getsPlotterProfitabilityWithExternalRevenueInternalTraceabilityAndNoInkCost() throws Exception {
        CreateInventoryItemResult roll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PLPA-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel API analítica",
                "PAPER",
                "METER",
                new BigDecimal("80.0000"),
                new BigDecimal("10.0000"),
                null,
                null,
                "PAPER",
                true,
                new InventoryAcquisitionCommand(
                        UUID.randomUUID(),
                        BigDecimal.ONE,
                        new BigDecimal("200000.00"),
                        null,
                        JOB_DATE,
                        "compra papel API"
                )
        ));
        String customerName = "Cliente API plotter G " + UUID.randomUUID();
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand(customerName));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor API plotter G " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                LocalDate.of(2099, 4, 20),
                sellerId,
                null,
                LocalDate.of(2099, 4, 5)
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Producto API plotter G",
                1,
                "Sudáfrica",
                "Negro",
                new BigDecimal("50000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        var order = createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                "ORD-PLGA-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                LocalDate.of(2099, 4, 8),
                LocalDate.of(2099, 4, 25),
                null
        ));

        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("8.0000"),
                new BigDecimal("15000.00"),
                "externo API G",
                PlotterJobType.EXTERNAL,
                null
        ));
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                JOB_DATE,
                roll.inventoryItemId(),
                new BigDecimal("5.0000"),
                new BigDecimal("8000.00"),
                "interno API G",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));

        mockMvc.perform(
                        get("/api/v1/plotter/profitability")
                                .param("fromDate", PERIOD_START.toString())
                                .param("toDate", PERIOD_END.toString())
                                .param("scope", "ALL")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ALL"))
                .andExpect(jsonPath("$.jobCount").value(2))
                .andExpect(jsonPath("$.externalJobCount").value(1))
                .andExpect(jsonPath("$.internalJobCount").value(1))
                .andExpect(jsonPath("$.totalPaperPrintedMeters").value(13.0))
                .andExpect(jsonPath("$.externalRevenue").value(120000.00))
                .andExpect(jsonPath("$.internalRevenue").value(40000.00))
                .andExpect(jsonPath("$.combinedRevenue").value(160000.00))
                .andExpect(jsonPath("$.totalPaperCost").value(200000.00))
                .andExpect(jsonPath("$.analyticalPlotterResult").value(-40000.00))
                .andExpect(jsonPath("$.inkCostRecorded").value(false))
                .andExpect(jsonPath("$.paperCostComplete").value(true))
                .andExpect(jsonPath("$.internalOrders[*].orderNumber", hasItem(order.orderNumber())))
                .andExpect(jsonPath("$.internalOrders[*].customerName", hasItem(customerName)))
                .andExpect(jsonPath("$.internalOrders[*].serviceValue", hasItem(40000.00)));

        mockMvc.perform(
                        get("/api/v1/plotter/profitability")
                                .param("fromDate", PERIOD_START.toString())
                                .param("toDate", PERIOD_END.toString())
                                .param("scope", "INTERNAL")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalRevenue").value(0.00))
                .andExpect(jsonPath("$.internalJobCount").value(1))
                .andExpect(jsonPath("$.internalRevenue").value(40000.00))
                .andExpect(jsonPath("$.analyticalPlotterResult").value(-160000.00));
    }

    @Test
    void returnsBadRequestWhenOnlyOneDateIsProvided() throws Exception {
        mockMvc.perform(
                        get("/api/v1/plotter/profitability")
                                .param("fromDate", "2099-04-01")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
