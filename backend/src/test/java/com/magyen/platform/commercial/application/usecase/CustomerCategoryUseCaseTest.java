package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerCategory;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.exception.QuotationDomainException;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
import com.magyen.platform.shared.testsupport.PlotterCustomerFixture;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CustomerCategoryUseCaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @Test
    void quotationFlowCreatesMagyenAndRejectsOtherCategories() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String name = "Cliente API " + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","category":"MAGYEN"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("MAGYEN"));

        mockMvc.perform(get("/api/v1/customers").param("category", "MAGYEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers[*].category", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("MAGYEN")
                )));

        var magyen = createCustomerUseCase.execute(new CreateCustomerCommand(
                "Magyen " + UUID.randomUUID(),
                CustomerCategory.MAGYEN
        ));
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor cat " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                magyen.customerId(),
                LocalDate.of(2099, 8, 20),
                sellerId,
                null,
                LocalDate.of(2099, 8, 1)
        ));
        assertEquals(magyen.customerId(), customerRepository.findById(magyen.customerId()).orElseThrow().getId());
        assertEquals(CustomerCategory.MAGYEN, magyen.category());

        UUID plotterCustomerId = PlotterCustomerFixture.create(createCustomerUseCase);
        assertThrows(QuotationDomainException.class, () -> createQuotationUseCase.execute(new CreateQuotationCommand(
                plotterCustomerId,
                LocalDate.of(2099, 8, 20),
                sellerId,
                null,
                LocalDate.of(2099, 8, 1)
        )));

        Customer unclassified = customerRepository.save(Customer.create(
                "Sin grupo " + UUID.randomUUID(),
                CustomerCategory.UNCLASSIFIED
        ));
        assertThrows(QuotationDomainException.class, () -> createQuotationUseCase.execute(new CreateQuotationCommand(
                unclassified.getId(),
                LocalDate.of(2099, 8, 20),
                sellerId,
                null,
                LocalDate.of(2099, 8, 1)
        )));

        var roll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "CAT-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel categoria",
                "PAPER",
                "METER",
                new BigDecimal("20.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("1000.00"),
                "PAPER",
                true
        ));
        assertThrows(PlotterDomainException.class, () -> createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                magyen.customerId(),
                null,
                LocalDate.of(2099, 8, 2),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("1000"),
                null,
                PlotterJobType.EXTERNAL,
                null
        )));
        assertThrows(PlotterDomainException.class, () -> createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                unclassified.getId(),
                null,
                LocalDate.of(2099, 8, 2),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("1000"),
                null,
                PlotterJobType.EXTERNAL,
                null
        )));
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                plotterCustomerId,
                null,
                LocalDate.of(2099, 8, 2),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("1000"),
                null,
                PlotterJobType.EXTERNAL,
                null
        ));

        addQuotationItemUseCase.execute(new com.magyen.platform.commercial.application.dto.AddQuotationItemCommand(
                quotation.quotationId(),
                "Camiseta",
                1,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("10000"),
                null
        ));
        approveQuotationUseCase.execute(
                new com.magyen.platform.commercial.application.dto.ApproveQuotationCommand(quotation.quotationId())
        );
        var order = createOrderFromQuotationUseCase.execute(
                new com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        null,
                        LocalDate.of(2099, 8, 2),
                        LocalDate.of(2099, 8, 20),
                        null
                )
        );
        createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                order.orderId(),
                LocalDate.of(2099, 8, 3),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("8000"),
                null,
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));
        assertEquals(
                CustomerCategory.MAGYEN,
                customerRepository.findById(magyen.customerId()).orElseThrow().getCategory()
        );
    }
}
