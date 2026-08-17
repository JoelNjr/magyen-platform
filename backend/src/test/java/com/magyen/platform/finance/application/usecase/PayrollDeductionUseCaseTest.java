package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelPayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionResult;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsResult;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.domain.PayrollDeductionType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PayrollDeductionUseCaseTest {

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreatePayrollDeductionUseCase createPayrollDeductionUseCase;

    @Autowired
    private GetPayrollDeductionsUseCase getPayrollDeductionsUseCase;

    @Autowired
    private CancelPayrollDeductionUseCase cancelPayrollDeductionUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void createsLoanAdvanceAndOtherDeductionsForFixedAndProductionEmployeesWithoutLedger() {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();
        CreatePayrollEmployeeResult fixed = createFixed("Fijo-Desc-" + suffix());
        CreatePayrollEmployeeResult production = createProduction("Prod-Desc-" + suffix());

        CreatePayrollDeductionResult loan = createDeduction(
                fixed.employeeId(),
                PayrollDeductionType.LOAN,
                "150000.00",
                "Préstamo"
        );
        CreatePayrollDeductionResult advance = createDeduction(
                fixed.employeeId(),
                PayrollDeductionType.ADVANCE,
                "40000.00",
                "Anticipo"
        );
        CreatePayrollDeductionResult other = createDeduction(
                production.employeeId(),
                PayrollDeductionType.OTHER,
                "25000.50",
                "Otro descuento"
        );

        assertEquals(PayrollDeductionType.LOAN, loan.type());
        assertEquals(PayrollDeductionType.ADVANCE, advance.type());
        assertEquals(PayrollDeductionType.OTHER, other.type());
        assertEquals(PayrollDeductionStatus.ACTIVE, loan.status());
        assertEquals(new BigDecimal("25000.50"), other.amount());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());

        GetPayrollDeductionsResult fixedHistory = getPayrollDeductionsUseCase.execute(
                new GetPayrollDeductionsQuery(fixed.employeeId(), null)
        );
        assertEquals(2, fixedHistory.deductions().size());
        assertEquals(2, fixedHistory.activeCount());
        assertEquals(new BigDecimal("190000.00"), fixedHistory.activeTotal());
    }

    @Test
    void rejectsZeroAndNegativeAmountsAndUnknownEmployee() {
        CreatePayrollEmployeeResult employee = createFixed("Monto-" + suffix());

        assertThrows(FinanceDomainException.class, () -> createDeduction(
                employee.employeeId(),
                PayrollDeductionType.LOAN,
                "0.00",
                "cero"
        ));
        assertThrows(FinanceDomainException.class, () -> createDeduction(
                employee.employeeId(),
                PayrollDeductionType.LOAN,
                "-10.00",
                "negativo"
        ));
        assertThrows(FinanceDomainException.class, () -> createDeduction(
                UUID.randomUUID(),
                PayrollDeductionType.LOAN,
                "10000.00",
                "inexistente"
        ));
    }

    @Test
    void cancelPreservesHistoryAndExcludesFromActiveTotals() {
        CreatePayrollEmployeeResult employee = createProduction("Cancel-Desc-" + suffix());
        CreatePayrollDeductionResult active = createDeduction(
                employee.employeeId(),
                PayrollDeductionType.LOAN,
                "80000.00",
                "activo"
        );
        CreatePayrollDeductionResult cancelled = createDeduction(
                employee.employeeId(),
                PayrollDeductionType.ADVANCE,
                "20000.00",
                "a cancelar"
        );

        cancelPayrollDeductionUseCase.execute(
                new CancelPayrollDeductionCommand(employee.employeeId(), cancelled.deductionId())
        );

        GetPayrollDeductionsResult history = getPayrollDeductionsUseCase.execute(
                new GetPayrollDeductionsQuery(employee.employeeId(), null)
        );
        GetPayrollDeductionsResult activeOnly = getPayrollDeductionsUseCase.execute(
                new GetPayrollDeductionsQuery(employee.employeeId(), PayrollDeductionStatus.ACTIVE)
        );

        assertEquals(2, history.deductions().size());
        assertTrue(history.deductions().stream()
                .anyMatch(item -> item.deductionId().equals(cancelled.deductionId())
                        && item.status() == PayrollDeductionStatus.CANCELLED));
        assertEquals(1, history.activeCount());
        assertEquals(new BigDecimal("80000.00"), history.activeTotal());
        assertEquals(1, activeOnly.deductions().size());
        assertEquals(active.deductionId(), activeOnly.deductions().getFirst().deductionId());
    }

    @Test
    void duplicateEmployeeDisplayNamesRemainAllowed() {
        String sharedName = "Ana Duplicada-" + suffix();
        CreatePayrollEmployeeResult first = createFixed(sharedName);
        CreatePayrollEmployeeResult second = createFixed(sharedName);

        assertEquals(sharedName, first.displayName());
        assertEquals(sharedName, second.displayName());
        assertTrue(!first.employeeId().equals(second.employeeId()));
    }

    private CreatePayrollDeductionResult createDeduction(
            UUID employeeId,
            PayrollDeductionType type,
            String amount,
            String description
    ) {
        return createPayrollDeductionUseCase.execute(new CreatePayrollDeductionCommand(
                employeeId,
                type,
                new BigDecimal(amount),
                LocalDate.of(2026, 8, 17),
                description
        ));
    }

    private CreatePayrollEmployeeResult createFixed(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.FIXED_PAYROLL,
                new BigDecimal("1500000.00"),
                LocalDate.of(2026, 8, 1),
                null
        ));
    }

    private CreatePayrollEmployeeResult createProduction(String name) {
        return createPayrollEmployeeUseCase.execute(new CreatePayrollEmployeeCommand(
                name,
                PayrollCompensationType.PRODUCTION_BASED,
                null,
                null,
                null
        ));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
