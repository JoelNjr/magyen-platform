package com.magyen.platform.finance.presentation;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PayrollCompensationType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PayrollDeductionApiContractTest {

    private static final Pattern DEDUCTION_ID_PATTERN =
            Pattern.compile("\"deductionId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void deductionEndpointsWorkWithoutCreatingFinanceExpense() throws Exception {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();
        CreatePayrollEmployeeResult employee = createPayrollEmployeeUseCase.execute(
                new CreatePayrollEmployeeCommand(
                        "API-Desc-" + UUID.randomUUID().toString().substring(0, 8),
                        PayrollCompensationType.FIXED_PAYROLL,
                        new BigDecimal("1500000.00"),
                        LocalDate.of(2026, 8, 1),
                        null
                )
        );

        mockMvc.perform(get("/api/v1/finance/payroll/employees/{employeeId}", employee.employeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canSell").value(true))
                .andExpect(jsonPath("$.canDoProduction").value(false));

        MvcResult created = mockMvc.perform(
                        post("/api/v1/finance/payroll/employees/{employeeId}/deductions", employee.employeeId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "LOAN",
                                          "amount": 120000.00,
                                          "deductionDate": "2026-08-17",
                                          "description": "Préstamo API"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("LOAN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.amount").value(120000.00))
                .andReturn();

        UUID deductionId = extractUuid(created.getResponse().getContentAsString());

        mockMvc.perform(get("/api/v1/finance/payroll/employees/{employeeId}/deductions", employee.employeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCount").value(1))
                .andExpect(jsonPath("$.activeTotal").value(120000.00));

        mockMvc.perform(patch(
                        "/api/v1/finance/payroll/employees/{employeeId}/deductions/{deductionId}/cancel",
                        employee.employeeId(),
                        deductionId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/finance/payroll/employees/{employeeId}/deductions", employee.employeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCount").value(0))
                .andExpect(jsonPath("$.activeTotal").value(0))
                .andExpect(jsonPath("$.deductions[0].status").value("CANCELLED"));

        mockMvc.perform(
                        post("/api/v1/finance/payroll/employees/{employeeId}/deductions", employee.employeeId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "ADVANCE",
                                          "amount": 0,
                                          "deductionDate": "2026-08-17"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    private UUID extractUuid(String body) {
        Matcher matcher = DEDUCTION_ID_PATTERN.matcher(body);
        assertTrue(matcher.find(), "Deduction id not found in response: " + body);
        return UUID.fromString(matcher.group(1));
    }
}
