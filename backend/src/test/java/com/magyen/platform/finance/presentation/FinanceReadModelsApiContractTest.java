package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class FinanceReadModelsApiContractTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 10);

    @MockitoBean
    private Clock clock;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void pendingOverdueUpcomingAndSummaryContracts() throws Exception {
        CreateRecurringFinancialObligationResult overdueObligation = createObligation(
                "Overdue-" + suffix(),
                "120000.00",
                9
        );
        CreateRecurringFinancialObligationResult upcomingObligation = createObligation(
                "Upcoming-" + suffix(),
                "800000.00",
                15
        );

        CreateRecurringFinancialObligationOccurrenceResult overdueOccurrence = createOccurrence(
                overdueObligation.obligationId(),
                LocalDate.of(2026, 8, 9)
        );
        CreateRecurringFinancialObligationOccurrenceResult upcomingOccurrence = createOccurrence(
                upcomingObligation.obligationId(),
                LocalDate.of(2026, 8, 15)
        );

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences").isArray())
                .andExpect(jsonPath("$.totalPendingAmount").isNumber())
                .andExpect(jsonPath("$.occurrences[*].occurrenceId").value(
                        hasItem(overdueOccurrence.occurrenceId().toString())
                ))
                .andExpect(jsonPath("$.occurrences[*].occurrenceId").value(
                        hasItem(upcomingOccurrence.occurrenceId().toString())
                ));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences").isArray())
                .andExpect(jsonPath("$.totalOverdueAmount").isNumber())
                .andExpect(jsonPath("$.occurrences[*].occurrenceId").value(
                        hasItem(overdueOccurrence.occurrenceId().toString())
                ))
                .andExpect(jsonPath("$.occurrences[?(@.occurrenceId=='"
                        + overdueOccurrence.occurrenceId() + "')].daysOverdue").value(hasItem(1)))
                .andExpect(jsonPath("$.occurrences[*].occurrenceId",
                        org.hamcrest.Matchers.not(hasItem(upcomingOccurrence.occurrenceId().toString()))));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/upcoming").param("daysAhead", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences").isArray())
                .andExpect(jsonPath("$.occurrences[*].occurrenceId").value(
                        hasItem(upcomingOccurrence.occurrenceId().toString())
                ))
                .andExpect(jsonPath("$.occurrences[*].occurrenceId",
                        org.hamcrest.Matchers.not(hasItem(overdueOccurrence.occurrenceId().toString()))));

        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("5000000.00"),
                        LocalDate.of(2026, 8, 5),
                        "SALES",
                        "API summary test",
                        null,
                        null,
                        null
                )
        );
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("3200000.00"),
                        LocalDate.of(2026, 8, 20),
                        "SERVICES",
                        "API summary test",
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(get("/api/v1/finance/summary")
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-08-01"))
                .andExpect(jsonPath("$.toDate").value("2026-08-31"))
                .andExpect(jsonPath("$.totalIncome").value(greaterThanOrEqualTo(5000000.00)))
                .andExpect(jsonPath("$.totalExpense").value(greaterThanOrEqualTo(3200000.00)))
                .andExpect(jsonPath("$.transactionCount").value(greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/" + upcomingOccurrence.occurrenceId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrenceId").value(upcomingOccurrence.occurrenceId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void readEndpointsValidateDatesAndDaysAhead() throws Exception {
        mockMvc.perform(get("/api/v1/finance/summary").param("toDate", "2026-08-31"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/summary")
                        .param("fromDate", "2026-08-31")
                        .param("toDate", "2026-08-01"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/summary")
                        .param("fromDate", "not-a-date")
                        .param("toDate", "2026-08-31"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/upcoming").param("daysAhead", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/upcoming").param("daysAhead", "367"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyReadStatesReturnZeroTotalsAndEmptyCollections() throws Exception {
        mockMvc.perform(get("/api/v1/finance/summary")
                        .param("fromDate", "2099-01-01")
                        .param("toDate", "2099-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0.00))
                .andExpect(jsonPath("$.totalExpense").value(0.00))
                .andExpect(jsonPath("$.netResult").value(0.00))
                .andExpect(jsonPath("$.transactionCount").value(0));
    }

    @Test
    void payStillWorksAlongsideReadEndpoints() throws Exception {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "PayRead-" + suffix(),
                "100000.00",
                15
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrence(
                obligation.obligationId(),
                LocalDate.of(2026, 8, 15)
        );

        mockMvc.perform(patch("/api/v1/finance/obligation-occurrences/" + occurrence.occurrenceId() + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paidAt":"2026-08-16T09:00:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/v1/finance/obligation-occurrences/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occurrences[*].occurrenceId",
                        org.hamcrest.Matchers.not(hasItem(occurrence.occurrenceId().toString()))));
    }

    private CreateRecurringFinancialObligationResult createObligation(
            String name,
            String amount,
            int dueDay
    ) {
        return createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        name,
                        RecurringObligationType.SERVICE,
                        new BigDecimal(amount),
                        RecurringObligationFrequency.MONTHLY,
                        dueDay,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
    }

    private CreateRecurringFinancialObligationOccurrenceResult createOccurrence(
            UUID obligationId,
            LocalDate dueDate
    ) {
        return createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(obligationId, dueDate, null)
        );
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
