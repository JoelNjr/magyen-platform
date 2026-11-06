package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
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

    @Test
    void explicitPaymentDateIsPersistedAndASecondPayDoesNotCreateAnotherExpense() throws Exception {
        String occurrenceId = createOccurrence("2026-10-15");
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        MvcResult paid = mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", occurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": "2026-10-31"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.dueDate").value("2026-10-15"))
                .andExpect(jsonPath("$.paidDate").value("2026-10-31T00:00:00"))
                .andReturn();

        String transactionId = com.jayway.jsonpath.JsonPath.read(
                paid.getResponse().getContentAsString(),
                "$.financialTransactionId"
        );
        FinancialTransaction transaction = financialTransactionRepository
                .findById(java.util.UUID.fromString(transactionId))
                .orElseThrow();
        assertEquals(LocalDate.of(2026, 10, 31), transaction.getTransactionDate());
        assertEquals(FinancialTransactionSourceType.RECURRING_OBLIGATION, transaction.getSourceType());
        assertEquals(java.util.UUID.fromString(occurrenceId), transaction.getSourceId());
        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/{occurrenceId}", occurrenceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-10-15"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.financialTransactionId").value(transactionId));

        mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", occurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": "2026-10-30"
                                        }
                                        """)
                )
                .andExpect(status().isConflict());

        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(
                transaction.getId(),
                financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.RECURRING_OBLIGATION,
                        java.util.UUID.fromString(occurrenceId)
                ).orElseThrow().getId()
        );
    }

    @Test
    void paymentDateInAPreviousMonthIsAcceptedByTheApi() throws Exception {
        String occurrenceId = createOccurrence("2026-10-15");

        mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", occurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": "2026-09-30"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-10-15"))
                .andExpect(jsonPath("$.paidDate").value("2026-09-30T00:00:00"));
    }

    @Test
    void invalidPaymentDateIsRejectedAndAMissingDateKeepsThePreviousContract() throws Exception {
        String invalidOccurrenceId = createOccurrence("2026-10-15");
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", invalidOccurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": "2026-02-31"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", invalidOccurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "paymentDate": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/{occurrenceId}", invalidOccurrenceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.financialTransactionId").value(nullValue()));
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());

        String omittedOccurrenceId = createOccurrence("2026-11-15");
        MvcResult omitted = mockMvc.perform(
                        patch("/api/v1/finance/obligation-occurrences/{occurrenceId}/pay", omittedOccurrenceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2026-11-15"))
                .andReturn();
        String omittedTransactionId = com.jayway.jsonpath.JsonPath.read(
                omitted.getResponse().getContentAsString(),
                "$.financialTransactionId"
        );
        String omittedPaidDate = com.jayway.jsonpath.JsonPath.read(
                omitted.getResponse().getContentAsString(),
                "$.paidDate"
        );
        assertEquals(LocalDate.now(), financialTransactionRepository
                .findById(java.util.UUID.fromString(omittedTransactionId))
                .orElseThrow()
                .getTransactionDate());
        assertEquals(true, omittedPaidDate.startsWith(LocalDate.now().toString()));
    }

    private String createOccurrence(String dueDate) throws Exception {
        MvcResult created = mockMvc.perform(
                        post("/api/v1/finance/obligation-occurrences")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "recurringObligationId": "%s",
                                          "dueDate": "%s"
                                        }
                                        """.formatted(obligation.obligationId(), dueDate))
                )
                .andExpect(status().isCreated())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.occurrenceId");
    }
}
