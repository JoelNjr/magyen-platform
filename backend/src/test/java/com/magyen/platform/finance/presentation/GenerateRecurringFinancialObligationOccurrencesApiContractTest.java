package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.DeactivateRecurringFinancialObligationUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class GenerateRecurringFinancialObligationOccurrencesApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createRecurringFinancialObligationUseCase;

    @Autowired
    private DeactivateRecurringFinancialObligationUseCase deactivateRecurringFinancialObligationUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;
    private UUID activeObligationId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        activeObligationId = createRecurringFinancialObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Internet-" + UUID.randomUUID().toString().substring(0, 8),
                        RecurringObligationType.SERVICE,
                        new BigDecimal("120000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        ).obligationId();

        UUID inactiveObligationId = createRecurringFinancialObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Antiguo-" + UUID.randomUUID().toString().substring(0, 8),
                        RecurringObligationType.OTHER,
                        new BigDecimal("10000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        10,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        ).obligationId();

        deactivateRecurringFinancialObligationUseCase.execute(
                new DeactivateRecurringFinancialObligationCommand(inactiveObligationId)
        );
    }

    @Test
    void generateEndpointIsIdempotentAndCreatesNoTransactions() throws Exception {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        MvcResult first = mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fromDate": "2026-08-01",
                                          "toDate": "2026-08-31"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedFrom").value("2026-08-01"))
                .andExpect(jsonPath("$.requestedTo").value("2026-08-31"))
                .andExpect(jsonPath("$.occurrencesCreated", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.occurrencesSkippedInactive", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath(
                        "$.createdOccurrences[*].recurringObligationId",
                        hasItem(activeObligationId.toString())
                ))
                .andExpect(jsonPath("$.createdOccurrences[*].status", hasItem("PENDING")))
                .andReturn();

        List<String> createdIds = com.jayway.jsonpath.JsonPath.read(
                first.getResponse().getContentAsString(),
                "$.createdOccurrences[?(@.recurringObligationId == '" + activeObligationId + "')].occurrenceId"
        );
        assertEquals(1, createdIds.size());

        mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fromDate": "2026-08-01",
                                          "toDate": "2026-08-31"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrencesAlreadyExisting", greaterThanOrEqualTo(1)));

        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void rejectsInvalidGenerationPayloads() throws Exception {
        mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "toDate": "2026-08-31"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fromDate": "2026-08-31",
                                          "toDate": "2026-08-01"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fromDate": "not-a-date",
                                          "toDate": "2026-08-31"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}
