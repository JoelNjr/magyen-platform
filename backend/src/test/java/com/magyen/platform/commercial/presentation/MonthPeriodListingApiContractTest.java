package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class MonthPeriodListingApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void quotationsListFiltersByCreationMonthAndAllowsUnfilteredHistory() throws Exception {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-api-month-" + UUID.randomUUID().toString().substring(0, 8)
        );
        var customer = createCustomerUseCase.execute(
                new CreateCustomerCommand("Cliente mes API " + UUID.randomUUID())
        );
        var march = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                LocalDate.of(2099, 3, 20),
                sellerId,
                "marzo",
                LocalDate.of(2099, 3, 15)
        ));
        var april = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                LocalDate.of(2099, 4, 20),
                sellerId,
                "abril",
                LocalDate.of(2099, 4, 15)
        ));

        mockMvc.perform(
                        get("/api/v1/quotations")
                                .param("fromDate", "2099-03-01")
                                .param("toDate", "2099-03-31")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotations[*].quotationId", hasItem(march.quotationId().toString())))
                .andExpect(jsonPath("$.quotations[*].quotationId", not(hasItem(april.quotationId().toString()))));

        mockMvc.perform(get("/api/v1/quotations").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotations[*].quotationId", hasItem(march.quotationId().toString())))
                .andExpect(jsonPath("$.quotations[*].quotationId", hasItem(april.quotationId().toString())));

        mockMvc.perform(
                        get("/api/v1/quotations")
                                .param("fromDate", "2099-03-01")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
