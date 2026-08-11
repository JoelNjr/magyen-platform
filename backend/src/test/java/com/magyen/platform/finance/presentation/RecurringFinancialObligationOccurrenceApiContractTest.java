package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RecurringFinancialObligationOccurrenceApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;
    private CreateRecurringFinancialObligationResult obligation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        obligation = createRecurringFinancialObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Internet",
                        RecurringObligationType.SERVICE,
                        new BigDecimal("120000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        "Internet del taller",
                        null
                )
        );
    }

    @Test
    void occurrenceLifecycleEndpointsAndLedgerGuarantees() throws Exception {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        MvcResult created = mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "recurringObligationId": "%s",
                                          "dueDate": "2026-08-15",
                                          "observation": null
                                        }
                                        """.formatted(obligation.obligationId()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occurrenceId").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.expectedAmount").value(120000.00))
                .andExpect(jsonPath("$.financialTransactionId").value(nullValue()))
                .andReturn();

        String occurrenceId = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(),
                "$.occurrenceId"
        );

        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/{occurrenceId}", occurrenceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrenceId").value(occurrenceId));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.occurrences[*].occurrenceId", hasItem(occurrenceId)));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences[*].occurrenceId", hasItem(occurrenceId)));

        mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "recurringObligationId": "%s",
                                          "dueDate": "2026-08-15"
                                        }
                                        """.formatted(obligation.obligationId()))
                )
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", occurrenceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.financialTransactionId").exists())
                .andExpect(jsonPath("$.transactionAmount").value(120000.00))
                .andExpect(jsonPath("$.transactionCategory").value("SERVICES"));

        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        mockMvc.perform(patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", occurrenceId))
                .andExpect(status().isConflict());

        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        mockMvc.perform(patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/cancel", occurrenceId))
                .andExpect(status().isBadRequest());

        MvcResult second = mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "recurringObligationId": "%s",
                                          "dueDate": "2026-09-15"
                                        }
                                        """.formatted(obligation.obligationId()))
                )
                .andExpect(status().isCreated())
                .andReturn();
        String secondId = com.jayway.jsonpath.JsonPath.read(
                second.getResponse().getContentAsString(),
                "$.occurrenceId"
        );

        mockMvc.perform(patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/cancel", secondId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/{occurrenceId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
